# Admin-only endpoints to issue, list, and revoke API keys
import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import generate_api_key, get_admin_key
from app.database import get_db
from app.models import ApiKey
from app.schemas import ApiKeyCreate, ApiKeyCreated, ApiKeyOut, ApiKeyUpdate

router = APIRouter(prefix="/api-keys", tags=["API Keys"], dependencies=[Depends(get_admin_key)])


@router.post("/", response_model=ApiKeyCreated, status_code=status.HTTP_201_CREATED)
async def create_api_key(payload: ApiKeyCreate, db: AsyncSession = Depends(get_db)):
    raw_key, key_hash = generate_api_key()
    api_key = ApiKey(name=payload.name, key_hash=key_hash)
    db.add(api_key)
    await db.commit()
    await db.refresh(api_key)
    return ApiKeyCreated(
        id=api_key.id,
        name=api_key.name,
        is_active=api_key.is_active,
        created_at=api_key.created_at,
        last_used_at=api_key.last_used_at,
        api_key=raw_key,
    )


@router.get("/", response_model=list[ApiKeyOut])
async def list_api_keys(db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(ApiKey).order_by(ApiKey.created_at.desc()))
    return result.scalars().all()


@router.put("/{key_id}", response_model=ApiKeyOut)
async def update_api_key(key_id: uuid.UUID, payload: ApiKeyUpdate, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(ApiKey).where(ApiKey.id == key_id))
    api_key = result.scalar_one_or_none()
    if api_key is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="API key not found")

    update_data = payload.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(api_key, field, value)

    await db.commit()
    await db.refresh(api_key)
    return api_key


@router.delete("/{key_id}", status_code=status.HTTP_200_OK)
async def revoke_api_key(key_id: uuid.UUID, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(ApiKey).where(ApiKey.id == key_id))
    api_key = result.scalar_one_or_none()
    if api_key is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="API key not found")
    api_key.is_active = False
    await db.commit()
    return {"detail": "API key revoked"}
