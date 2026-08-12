"""Maintainable deterministic interview question catalogs."""

TECHNICAL_QUESTIONS: dict[str, dict[str, tuple[str, ...]]] = {
    "Java": {
        "EASY": ("What is the difference between JDK, JRE, and JVM?", "Explain the core OOP principles in Java."),
        "MEDIUM": ("What is the difference between HashMap and ConcurrentHashMap?", "Explain exception handling in Java."),
        "HARD": ("Explain JVM memory management and garbage collection.", "How would you design a thread-safe service in Java?"),
    },
    "Python": {
        "EASY": ("What are the main built-in collection types in Python?", "What is the difference between a list and a tuple?"),
        "MEDIUM": ("Explain decorators and a practical use for them.", "How does Python manage memory?"),
        "HARD": ("Explain the GIL and its effect on concurrency.", "How would you profile and optimize a slow Python service?"),
    },
    "JavaScript": {
        "EASY": ("What is the difference between let, const, and var?", "What is a JavaScript promise?"),
        "MEDIUM": ("Explain closures and a practical use for them.", "How does the JavaScript event loop work?"),
        "HARD": ("How would you diagnose memory leaks in a JavaScript application?", "Explain microtasks and macrotasks in event-loop scheduling."),
    },
    "TypeScript": {
        "EASY": ("What benefits does TypeScript add to JavaScript?", "What is an interface in TypeScript?"),
        "MEDIUM": ("Explain union types and type narrowing.", "When would you use generics in TypeScript?"),
        "HARD": ("How would you design a type-safe public API with advanced TypeScript types?", "Explain conditional and mapped types."),
    },
    "Spring Boot": {
        "EASY": ("What is dependency injection?", "What is the purpose of @RestController?"),
        "MEDIUM": ("Explain Spring Boot auto-configuration.", "How do you validate and handle errors in a REST API?"),
        "HARD": ("Explain the Spring Security authentication flow.", "How would you make a Spring Boot service resilient under partial failures?"),
    },
    "React": {
        "EASY": ("What are props and state in React?", "What problem do React hooks solve?"),
        "MEDIUM": ("What is the difference between useEffect and useMemo?", "How do controlled components work?"),
        "HARD": ("How would you diagnose unnecessary React renders?", "How would you structure state in a large React application?"),
    },
    "Node.js": {
        "EASY": ("What is the Node.js event loop?", "What is middleware?"),
        "MEDIUM": ("Explain asynchronous programming in Node.js.", "How do streams help with large data processing?"),
        "HARD": ("How would you scale a CPU-intensive Node.js workload?", "How do you prevent event-loop blocking in production?"),
    },
    "SQL": {
        "EASY": ("What is a JOIN?", "What is database normalization?"),
        "MEDIUM": ("What are database indexes and what trade-offs do they introduce?", "Explain transaction isolation levels."),
        "HARD": ("How would you diagnose and optimize a slow query plan?", "How would you prevent concurrency anomalies in a transactional workflow?"),
    },
    "PostgreSQL": {
        "EASY": ("What is a primary key in PostgreSQL?", "What is the difference between WHERE and HAVING?"),
        "MEDIUM": ("How do PostgreSQL indexes improve query performance?", "Explain transactions and isolation in PostgreSQL."),
        "HARD": ("How would you investigate PostgreSQL lock contention?", "Explain MVCC and how it affects database maintenance."),
    },
    "Docker": {
        "EASY": ("What is a Docker image?", "What is the difference between an image and a container?"),
        "MEDIUM": ("How do Docker layers affect build caching?", "How should secrets be handled in containerized applications?"),
        "HARD": ("How would you harden and optimize a production container image?", "How would you diagnose networking problems between containers?"),
    },
    "Git": {
        "EASY": ("What is a Git branch?", "What is the purpose of a commit?"),
        "MEDIUM": ("What is the difference between merge and rebase?", "How do you resolve a merge conflict safely?"),
        "HARD": ("How would you recover a lost commit using the reflog?", "How would you manage a complex long-running branch strategy?"),
    },
    "REST API": {
        "EASY": ("What makes an API RESTful?", "What are common HTTP methods used by REST APIs?"),
        "MEDIUM": ("How would you design pagination and filtering for a REST API?", "How should a REST API communicate validation errors?"),
        "HARD": ("How would you make a write API idempotent?", "How would you evolve a public API without breaking clients?"),
    },
}

GENERAL_TECHNICAL: dict[str, tuple[str, ...]] = {
    "EASY": (
        "What is the difference between a process and a thread?", "What is an API?", "What is version control?",
        "What is the purpose of automated testing?", "What is the difference between a stack and a queue?",
        "What is a database transaction?", "What does HTTP status code 404 mean?", "What is dependency injection?",
        "What is the difference between frontend and backend development?", "What is a unit test?",
        "What is caching?", "What is a data structure?", "What is source control branching?", "What is a REST endpoint?",
        "Why are code reviews useful?", "What is continuous integration?", "What is an environment variable?",
        "What is logging?", "What is an algorithm?", "Why should passwords be hashed?",
    ),
    "MEDIUM": (
        "How would you design error handling for a web service?", "Explain the trade-offs between monoliths and microservices.",
        "How would you investigate a slow API?", "What makes an operation idempotent?", "How do you avoid race conditions?",
        "Explain optimistic and pessimistic locking.", "How would you structure integration tests?", "What are common caching invalidation strategies?",
        "How do authentication and authorization differ?", "How would you design pagination for a large dataset?",
        "What are the trade-offs of synchronous and asynchronous processing?", "How do you manage application configuration safely?",
        "How would you approach a production incident?", "What information belongs in application logs?",
        "How do database indexes affect writes?", "What makes a useful API error response?", "How do you prevent duplicate requests?",
        "What is eventual consistency?", "How would you review an unfamiliar codebase?", "How do you choose test boundaries?",
    ),
    "HARD": (
        "How would you design a highly available service?", "How would you reason about consistency in a distributed system?",
        "Design a strategy for zero-downtime database migrations.", "How would you isolate and recover from cascading failures?",
        "Explain backpressure and where it is useful.", "How would you design an idempotent distributed workflow?",
        "How do you evaluate partitioning strategies for a large dataset?", "How would you diagnose intermittent latency spikes?",
        "Explain the trade-offs of event-driven architecture.", "How would you protect a service from retry storms?",
        "How would you design observability for a distributed transaction?", "When would you choose strong consistency over availability?",
        "How do you reason about concurrency correctness?", "How would you evolve a high-traffic API safely?",
        "How would you plan capacity for an unpredictable workload?", "Explain failure domains in cloud architecture.",
        "How would you validate a disaster-recovery plan?", "How do you manage schema compatibility in event streams?",
        "How would you secure service-to-service communication?", "How do you decide where caching belongs in a system?",
    ),
}

HR_QUESTIONS: dict[str, tuple[str, ...]] = {
    "EASY": ("Tell me about yourself.", "What are your strengths?", "Why are you interested in this role?", "What motivates you at work?", "How do you organize your work?", "What type of team environment helps you succeed?", "What are you looking for in your next role?", "How do you respond to feedback?"),
    "MEDIUM": ("What is one area you are working to improve?", "Why should we hire you?", "Describe a challenge you faced in a project.", "Tell me about a time you worked in a team.", "Describe a disagreement and how you handled it.", "Tell me about a goal you achieved.", "How do you prioritize competing deadlines?", "Describe a time you took ownership of a problem."),
    "HARD": ("Tell me about a failure and what you changed afterward.", "Describe a decision you made with incomplete information.", "How have you influenced a team without formal authority?", "Describe a time you challenged an established approach.", "Where do you see yourself in three years?", "Tell me about an ethical concern you encountered at work.", "Describe a high-pressure situation and how you handled it.", "What difficult feedback have you received and acted on?"),
}

PROJECT_QUESTIONS: tuple[str, ...] = (
    "Explain one of your most important projects.", "What problem did the project solve?",
    "What technologies did you use and why?", "What was the most difficult technical issue?",
    "How did you test the project?", "What would you improve if you rebuilt it?",
    "How did you design the database or API?", "What security considerations did you handle?",
)
