from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import date, datetime
import uuid

class ProfileBase(BaseModel):
    name: Optional[str] = None
    profile_picture_url: Optional[str] = None
    english_level: Optional[str] = None

class ProfileUpdate(ProfileBase):
    pass

class ProfileRead(ProfileBase):
    id: uuid.UUID
    model_config = ConfigDict(from_attributes=True)

class User(BaseModel):
    id: str
    email: str
    
class UserCreate(BaseModel):
    email: str
    password: str
    
class UserResponse(BaseModel):
    id: uuid.UUID
    email: str
    
class Token(BaseModel):
    access_token: str
    token_type: str

# Nuevo modelo para la respuesta de Login
class UserLoginResponse(BaseModel):
    user: UserResponse
    session: Token
