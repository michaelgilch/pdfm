"""Application configuration.

Loaded from ``config.toml`` in the working directory, with environment-variable
overrides under the ``PDFMAN_`` prefix (e.g. ``PDFMAN_LIBRARY__DIR=/path``).
All settings have defaults, so the app runs with no config file present.
"""

from __future__ import annotations

from functools import lru_cache
from pathlib import Path

from pydantic import BaseModel
from pydantic_settings import (
    BaseSettings,
    PydanticBaseSettingsSource,
    SettingsConfigDict,
    TomlConfigSettingsSource,
)


class LibrarySettings(BaseModel):
    dir: Path = Path("/data/pdf-library")
    recursive: bool = True


class ScanSettings(BaseModel):
    interval_minutes: int = 0
    on_startup: bool = True


class ServerSettings(BaseModel):
    host: str = "127.0.0.1"
    port: int = 8000


class UISettings(BaseModel):
    default_view: str = "grid"


class FeatureSettings(BaseModel):
    extract_fulltext: bool = False


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        toml_file="config.toml",
        env_prefix="PDFMAN_",
        env_nested_delimiter="__",
        extra="ignore",
    )

    library: LibrarySettings = LibrarySettings()
    scan: ScanSettings = ScanSettings()
    server: ServerSettings = ServerSettings()
    ui: UISettings = UISettings()
    features: FeatureSettings = FeatureSettings()

    # Fixed runtime locations (not user-configurable).
    data_dir: Path = Path("data")

    @property
    def db_path(self) -> Path:
        return self.data_dir / "pdfman.db"

    @property
    def thumbs_dir(self) -> Path:
        return self.data_dir / "thumbs"

    @classmethod
    def settings_customise_sources(
        cls,
        settings_cls: type[BaseSettings],
        init_settings: PydanticBaseSettingsSource,
        env_settings: PydanticBaseSettingsSource,
        dotenv_settings: PydanticBaseSettingsSource,
        file_secret_settings: PydanticBaseSettingsSource,
    ) -> tuple[PydanticBaseSettingsSource, ...]:
        # Precedence: explicit init args > env vars > config.toml > defaults.
        return (init_settings, env_settings, TomlConfigSettingsSource(settings_cls))


@lru_cache
def get_settings() -> Settings:
    return Settings()
