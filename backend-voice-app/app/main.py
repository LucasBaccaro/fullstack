from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.routers import profile, topics, auth, openai, progress

app = FastAPI(
    title="Backend Voice App",
    description="API for the voice-based learning mobile application.",
    version="1.0.0"
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # You should restrict this to your app's domain in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(profile.router, prefix="/profile", tags=["Profile"])
app.include_router(topics.router, tags=["Topics"])
app.include_router(auth.router, prefix="/auth", tags=["Authentication"])
app.include_router(openai.router, tags=["OpenAI"])
app.include_router(progress.router, tags=["Progress"])

@app.get("/")
def read_root():
    return {"message": "Welcome to the Voice App API"}
