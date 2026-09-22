# Seed script: creates tables and inserts sample Task and ApiKey records
import asyncio

from dotenv import load_dotenv

load_dotenv('.env_b053751c0f39d3e4', override=True)

from sqlalchemy import select

from app.core.security import generate_api_key
from app.database import AsyncSessionLocal, Base, engine
from app.models import ApiKey, Task, TaskPriority, TaskStatus


async def seed():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all, checkfirst=True)

    async with AsyncSessionLocal() as session:
        result = await session.execute(select(Task))
        existing_tasks = result.scalars().all()
        if len(existing_tasks) == 0:
            tasks = [
                Task(title="Set up project repo", description="Initialize FastAPI project", status=TaskStatus.COMPLETED, priority=TaskPriority.HIGH),
                Task(title="Design database schema", description="Design Task and ApiKey tables", status=TaskStatus.COMPLETED, priority=TaskPriority.HIGH),
                Task(title="Implement CRUD endpoints", description="Build Task CRUD API", status=TaskStatus.IN_PROGRESS, priority=TaskPriority.MEDIUM),
                Task(title="Integrate Kafka events", description="Publish task lifecycle events", status=TaskStatus.TODO, priority=TaskPriority.MEDIUM),
                Task(title="Write tests", description="Cover CRUD and auth scenarios", status=TaskStatus.TODO, priority=TaskPriority.LOW),
            ]
            session.add_all(tasks)

        result = await session.execute(select(ApiKey))
        existing_keys = result.scalars().all()
        if len(existing_keys) == 0:
            raw_key, key_hash = generate_api_key()
            api_key = ApiKey(name="seed-default-key", key_hash=key_hash)
            session.add(api_key)
            await session.commit()
            print(f"Seed API key created. Raw key (save it, shown once): {raw_key}")
        else:
            await session.commit()

    print("Seed complete.")


if __name__ == "__main__":
    asyncio.run(seed())
