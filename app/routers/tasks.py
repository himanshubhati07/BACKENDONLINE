# CRUD endpoints for Task entity, protected by API key, publishing Kafka events
import logging
import uuid

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import get_current_api_key
from app.database import get_db
from app.kafka.producer import publish_task_event
from app.models import ApiKey, Task
from app.schemas import TaskCreate, TaskListOut, TaskOut, TaskUpdate

logger = logging.getLogger("routers.tasks")

router = APIRouter(prefix="/tasks", tags=["Tasks"], dependencies=[Depends(get_current_api_key)])


@router.post("", response_model=TaskOut, status_code=status.HTTP_201_CREATED)
async def create_task(payload: TaskCreate, db: AsyncSession = Depends(get_db)):
    task = Task(
        title=payload.title,
        description=payload.description,
        status=payload.status,
        priority=payload.priority,
    )
    db.add(task)
    await db.commit()
    await db.refresh(task)

    published = await publish_task_event(
        "TASK_CREATED",
        {
            "id": str(task.id),
            "title": task.title,
            "status": task.status.value,
            "priority": task.priority.value,
        },
    )
    if not published:
        logger.error("TASK_CREATED event failed to publish for task %s", task.id)

    return task


@router.get("", response_model=TaskListOut)
async def list_tasks(
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: AsyncSession = Depends(get_db),
):
    total_result = await db.execute(select(func.count()).select_from(Task))
    total = total_result.scalar_one()

    result = await db.execute(
        select(Task).order_by(Task.created_at.desc()).offset(offset).limit(limit)
    )
    items = result.scalars().all()

    return TaskListOut(items=items, total=total, limit=limit, offset=offset)


@router.get("/{task_id}", response_model=TaskOut)
async def get_task(task_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Task).where(Task.id == task_id))
    task = result.scalar_one_or_none()
    if task is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")
    return task


@router.put("/{task_id}", response_model=TaskOut)
async def update_task(task_id: uuid.UUID, payload: TaskUpdate, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Task).where(Task.id == task_id))
    task = result.scalar_one_or_none()
    if task is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    update_data = payload.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(task, field, value)

    await db.commit()
    await db.refresh(task)

    published = await publish_task_event(
        "TASK_UPDATED",
        {
            "id": str(task.id),
            "title": task.title,
            "status": task.status.value,
            "priority": task.priority.value,
        },
    )
    if not published:
        logger.error("TASK_UPDATED event failed to publish for task %s", task.id)

    return task


@router.delete("/{task_id}", status_code=status.HTTP_200_OK)
async def delete_task(task_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Task).where(Task.id == task_id))
    task = result.scalar_one_or_none()
    if task is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    task_id_str = str(task.id)
    await db.delete(task)
    await db.commit()

    published = await publish_task_event("TASK_DELETED", {"id": task_id_str})
    if not published:
        logger.error("TASK_DELETED event failed to publish for task %s", task_id_str)

    return {"detail": "Task deleted"}
