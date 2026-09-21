# Unit tests for pure logic in app.core.security (no DB, no server)
import hashlib

from app.core.security import generate_api_key, hash_key


def test_hash_key_is_sha256_hex():
    raw = "some-raw-key-value"
    hashed = hash_key(raw)
    assert hashed == hashlib.sha256(raw.encode()).hexdigest()
    assert len(hashed) == 64


def test_hash_key_deterministic():
    raw = "consistent-key"
    assert hash_key(raw) == hash_key(raw)


def test_generate_api_key_returns_raw_and_hash():
    raw_key, key_hash = generate_api_key()
    assert isinstance(raw_key, str)
    assert isinstance(key_hash, str)
    assert len(raw_key) > 20
    assert key_hash == hash_key(raw_key)


def test_generate_api_key_unique_each_call():
    raw1, hash1 = generate_api_key()
    raw2, hash2 = generate_api_key()
    assert raw1 != raw2
    assert hash1 != hash2
