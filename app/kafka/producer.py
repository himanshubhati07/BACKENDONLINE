# Kafka producer helper for publishing task events. Failures are logged, never silent.
import json
import logging
import os

from dotenv import load_dotenv

load_dotenv('.env_6fb25fff-dbb0-4770-a1c9-7ca2debd1b59', override=True)

from aiokafka import AIOKafkaProducer
from aiokafka.errors import KafkaError

logger = logging.getLogger("kafka.producer")

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
KAFKA_TASK_EVENTS_TOPIC = os.getenv("KAFKA_TASK_EVENTS_TOPIC", "task-events")

_producer: AIOKafkaProducer | None = None


async def get_producer() -> AIOKafkaProducer | None:
    global _producer
    if _producer is None:
        try:
            _producer = AIOKafkaProducer(
                bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
                value_serializer=lambda v: json.dumps(v, default=str).encode("utf-8"),
            )
            await _producer.start()
        except Exception as exc:  # noqa: BLE001
            logger.error("Failed to start Kafka producer: %s", exc)
            _producer = None
    return _producer


async def stop_producer() -> None:
    global _producer
    if _producer is not None:
        try:
            await _producer.stop()
        except Exception as exc:  # noqa: BLE001
            logger.error("Error stopping Kafka producer: %s", exc)
        finally:
            _producer = None


async def publish_task_event(event_type: str, payload: dict) -> bool:
    """Publish a task event to the task-events topic. Returns True on success."""
    producer = await get_producer()
    if producer is None:
        logger.error("Kafka producer unavailable; could not publish event %s", event_type)
        return False
    message = {"event_type": event_type, "data": payload}
    try:
        await producer.send_and_wait(KAFKA_TASK_EVENTS_TOPIC, message)
        return True
    except KafkaError as exc:
        logger.error("Failed to publish Kafka event %s: %s", event_type, exc)
        return False
    except Exception as exc:  # noqa: BLE001
        logger.error("Unexpected error publishing Kafka event %s: %s", event_type, exc)
        return False


async def check_kafka_health() -> bool:
    try:
        producer = await get_producer()
        if producer is None:
            return False
        return True
    except Exception:  # noqa: BLE001
        return False
