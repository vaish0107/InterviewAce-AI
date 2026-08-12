from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    ai_service_host: str = Field(default="127.0.0.1")
    ai_service_port: int = Field(default=8000, ge=1, le=65535)
    max_resume_size: int = Field(default=5_242_880, gt=0)
    gemini_api_key: str = Field(default="")
    gemini_model: str = Field(default="gemini-3.5-flash")

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=False,
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
