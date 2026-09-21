# FastAPI application entrypoint: routers, CORS, health check
import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import text

from app.config import API_PREFIX, PROJECT_NAME, PROJECT_VERSION
from app.database import AsyncSessionLocal
from app.kafka.producer import check_kafka_health, stop_producer
from app.routers import api_keys, tasks

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("app.main")

app = FastAPI(
    title=PROJECT_NAME,
    version=PROJECT_VERSION,
    description=(
        "Task Management API secured with API Key authentication "
        "(header: X-API-Key). Publishes TASK_CREATED, TASK_UPDATED, "
        "TASK_DELETED events to Kafka topic 'task-events'."
    ),
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_keys.router, prefix=API_PREFIX)
app.include_router(tasks.router, prefix=API_PREFIX)


@app.on_event("shutdown")
async def shutdown_event():
    await stop_producer()


@app.get("/health", tags=["Health"])
async def health_check():
    db_status = "ok"
    try:
        async with AsyncSessionLocal() as session:
            await session.execute(text("SELECT 1"))
    except Exception as exc:  # noqa: BLE001
        logger.error("Database health check failed: %s", exc)
        db_status = "error"

    kafka_ok = await check_kafka_health()
    kafka_status = "ok" if kafka_ok else "error"

    overall = "ok" if db_status == "ok" and kafka_status == "ok" else "degraded"

    return {
        "status": overall,
        "database": db_status,
        "kafka": kafka_status,
    }


@app.get("/", tags=["Health"])
async def root():
    return {"message": f"{PROJECT_NAME} v{PROJECT_VERSION} is running"}
