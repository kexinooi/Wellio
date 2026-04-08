# Wellio

Recommended repository structure
wellio-app/
│
├── frontend/ (Flutter / UI)
├── backend/ (API / server)
├── database/ (schemas, configs)
├── docs/ (API docs, diagrams)
└── README.md
Person 1 — Frontend (Mobile App UI)
Responsibility:
• Build the app interface
Tasks:
• Login / signup screens
• Dashboard UI
• Mood logging UI
• Charts / visualization
• Navigation / routing
Tech:
• Flutter (recommended)
Output:
• Working UI connected to mock or real API
Person 2 — Backend (API & Server)
Responsibility:
• Handle data + business logic
Tasks:
• User authentication
• REST API endpoints:
o login/register
o save mood logs
o fetch data
• Connect to database
• Calendar API integration (e.g. Google Calendar)
Tech:
• Node.js / Flask / FastAPI
Output:
• Working APIs tested via Postman
Person 3 — Database + Data Logic
Responsibility:
• Data modeling + analysis logic
Tasks:
• Design database schema:
o users
o mood logs
o academic schedule
• Implement queries
• Write logic for:
o trend analysis
o workload vs mood correlation
o stress detection rules
Tech:
• Firebase Firestore or MySQL/PostgreSQL
Output:
• Structured, optimized data storage + analysis functions
Person 4 — Integration + Notifications + Testing
Responsibility:
• Glue everything together
Tasks:
• Connect frontend ↔ backend APIs
• Handle API calls in app
• Implement notifications/alerts
• Test full system flow
• Debug integration issues
• Assist where needed
Tech:
• Flutter API integration / HTTP
• Notification services (Firebase Cloud Messaging if needed)
Output:
• End-to-end working app
Collaboration rules (to avoid chaos)
These rules are critical:
1. Never push directly to main
Always use branches + PRs
2. Define API contract early
Example:
POST /login
POST /mood
GET /mood/{user_id}
GET /calendar
Frontend and backend must agree on this
3. Frequent syncing
• Don’t wait until the end
• Merge regularly
• Pull updates before working
4. One feature = one branch (optional but clean)
Example:
• feature/login
• feature/dashboard