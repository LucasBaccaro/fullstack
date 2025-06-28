
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    SUPABASE_URL: str
    SUPABASE_KEY: str
    SUPABASE_JWT_SECRET: str

    model_config = SettingsConfigDict(env_file='.env', case_sensitive=True, extra='ignore')

settings = Settings()
