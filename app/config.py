# Application configuration loaded from environment variables
import os
from dotenv import load_dotenv

load_dotenv('.env_6fb25fff-dbb0-4770-a1c9-7ca2debd1b59', override=True)

API_PREFIX = os.getenv("API_PREFIX", "/api/v1")
PROJECT_NAME = os.getenv("PROJECT_NAME", "Task Management API")
PROJECT_VERSION = os.getenv("PROJECT_VERSION", "0.1.0")
