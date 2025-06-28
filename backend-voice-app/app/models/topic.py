
from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional

class TopicRead(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    prompt_context: str
    difficulty_level: Optional[str] = None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)

class UserProgressCreate(BaseModel):
    topic_id: int
