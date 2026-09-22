# Application configuration loaded from environment variables
import os
from dotenv import load_dotenv

load_dotenv('.env_b053751c0f39d3e4', override=True)

API_PREFIX = os.getenv("API_PREFIX", "/api/v1")
PROJECT_NAME = os.getenv("PROJECT_NAME", "Task Management API")
PROJECT_VERSION = os.getenv("PROJECT_VERSION", "0.1.0")
