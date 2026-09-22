# Pydantic v2 schemas for Task and ApiKey
import uuid
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, Field

from app.models import TaskStatus, TaskPriority


# ---------------- Task ----------------

class TaskBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=200)
    description: Optional[str] = None
    status: TaskStatus = TaskStatus.TODO
    priority: TaskPriority = TaskPriority.MEDIUM


class TaskCreate(TaskBase):
    pass


class TaskUpdate(BaseModel):
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = None
    status: Optional[TaskStatus] = None
    priority: Optional[TaskPriority] = None


class TaskOut(TaskBase):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    created_at: datetime
    updated_at: datetime


class TaskListOut(BaseModel):
    items: list[TaskOut]
    total: int
    limit: int
    offset: int


# ---------------- ApiKey ----------------

class ApiKeyCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)


class ApiKeyUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    is_active: Optional[bool] = None


class ApiKeyOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    name: str
    is_active: bool
    created_at: datetime
    last_used_at: Optional[datetime] = None


class ApiKeyCreated(ApiKeyOut):
    api_key: str
