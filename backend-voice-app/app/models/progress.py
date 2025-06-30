from pydantic import BaseModel, Field
from typing import List, Optional
from uuid import UUID
from datetime import datetime

class GrammarPoint(BaseModel):
    """Representa un punto gramatical específico practicado en la sesión."""
    point: str = Field(..., description="El concepto gramatical, ej: 'Past Simple Tense'")
    examples: List[str] = Field(default_factory=list, description="Ejemplos de la conversación.")
    status: str = Field("practiced", description="Estado del aprendizaje, ej: 'practiced', 'needs_review'")

class ProgressLog(BaseModel):
    """Modelo para registrar el progreso de una sesión de tutoría de inglés."""
    id: Optional[int] = Field(None, description="ID autoincremental de la base de datos.")
    user_id: UUID = Field(..., description="ID del usuario al que pertenece el registro.")
    session_date: datetime = Field(default_factory=datetime.utcnow, description="Fecha y hora UTC de la sesión.")
    duration_minutes: int = Field(..., gt=0, description="Duración de la sesión en minutos.")
    topics_discussed: List[str] = Field(default_factory=list, description="Lista de temas tratados en la conversación.")
    new_vocabulary: List[str] = Field(default_factory=list, description="Lista de nuevo vocabulario aprendido o usado.")
    grammar_points: List[GrammarPoint] = Field(default_factory=list, description="Puntos gramaticales específicos practicados.")
    ai_summary: str = Field(..., description="Resumen generado por la IA sobre el desempeño del usuario.")
    suggested_level: str = Field(..., description="Nivel de inglés sugerido por la IA para esta sesión.")

    class Config:
        orm_mode = True


class ProgressLogCreate(BaseModel):
    session_date: datetime
    duration_minutes: int
    topics_discussed: List[str]
    new_vocabulary: List[str]
    grammar_points: List[GrammarPoint]
    ai_summary: str
    suggested_level: str