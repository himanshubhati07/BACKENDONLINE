# API key based authentication (business + admin) utilities
import hashlib
import os
import secrets
from datetime import datetime

from dotenv import load_dotenv

load_dotenv('.env_b053751c0f39d3e4', override=True)

from fastapi import Depends, HTTPException, status
from fastapi.security import APIKeyHeader
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models import ApiKey

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)
admin_key_header = APIKeyHeader(name="X-Admin-Key", auto_error=False)

ADMIN_API_KEY = os.getenv("ADMIN_API_KEY", "")


def hash_key(raw: str) -> str:
    return hashlib.sha256(raw.encode()).hexdigest()


def generate_api_key() -> tuple[str, str]:
    raw_key = secrets.token_urlsafe(32)
    key_hash = hash_key(raw_key)
    return raw_key, key_hash


async def get_current_api_key(
    key: str | None = Depends(api_key_header),
    db: AsyncSession = Depends(get_db),
) -> ApiKey:
    if not key:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing API key")

    key_hash = hash_key(key)
    result = await db.execute(select(ApiKey).where(ApiKey.key_hash == key_hash))
    api_key_row = result.scalar_one_or_none()

    if api_key_row is None or not api_key_row.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or inactive API key")

    api_key_row.last_used_at = datetime.utcnow()
    await db.commit()
    await db.refresh(api_key_row)
    return api_key_row


def get_admin_key(admin_key: str | None = Depends(admin_key_header)) -> None:
    if not admin_key or not secrets.compare_digest(admin_key, ADMIN_API_KEY):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or missing admin key")
