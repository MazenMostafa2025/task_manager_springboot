**🧭 Project Overview**

A collaborative project and task management system built with Spring Boot.
It enables secure authentication, role-based access, project and task organization, comments, tags, and AI-powered task analysis using Google Gemini.
The system also integrates with RabbitMQ, Prometheus, and Grafana for asynchronous messaging and performance monitoring.

**⚙️ Architecture**

• Modular, layered Spring Boot structure

• RESTful API backend

• Token-based authentication

• Dockerized monitoring and messaging services

Main Layers:

• Controller: Handles REST API requests

• Service: Contains business logic

• Repository: Handles database operations

• Entity & DTO: Data modeling and transfer

**✨ Key Features**
**🔐 Authentication & Security**

• User registration and login with encrypted passwords

• JWT-based authentication (access and refresh tokens)

• Role-based access control (Admin, Team Leader, Member)

• Token refresh functionality

**🧑‍🤝‍🧑 User Management**

• Create and manage user accounts

• Check for existing usernames or emails

• Enable/disable user accounts

• Load user details for authentication

**🗂️ Project Management**

• Create, update, and delete projects

• Assign projects to owners

• Search and list projects by name

• Enforce ownership and admin permissions

**✅ Task Management**

• Create and update tasks within projects

• Assign users and tags to tasks

• Track task status and due dates

• Filter tasks by user, project, or status

• Automatically detect overdue tasks

• Send task reminders and notifications

**💬 Comments**

• Add and view comments on tasks

• Delete comments (only by author or admin)

• View all comments for a specific task

**🏷️ Tags**

• Create, update, and delete tags

• Prevent duplicate tag creation

• Assign multiple tags to tasks

**📬 Email Notifications**

• Send email alerts for new task assignments

• Simple text-based email delivery

• Integration with RabbitMQ for async processing

**🤖 AI Task Advisor (Gemini Integration)**

• Uses Google Vertex AI – Gemini 1.5 model

• Analyzes project tasks and suggests execution order

• Detects dependencies and bottlenecks

• Generates concise recommendations for project managers

**🐇 RabbitMQ Integration**

• Asynchronous messaging system for task events

• Supports scalable notification and real-time updates

• Optional integration with email and WebSocket systems

**📊 Monitoring & Metrics**

• Integrated Prometheus for metrics collection

• Grafana dashboards for visualization

• Metrics exposed through Spring Boot Actuator (/actuator/prometheus)

**🧪 Testing & Quality**

• Unit tests for core service and utility layers

• Integration tests covering REST endpoints and database logic

• Automated testing and build verification via GitHub Actions CI

**🧱 Tech Stack**
**Category	Technology**
• Backend Framework	Spring Boot
• Database	Spring Data JPA
• Security	Spring Security + JWT
• AI Integration	Google Vertex AI (Gemini)
• Messaging	RabbitMQ
• Email	JavaMailSender
• Monitoring	Prometheus + Grafana
• Build Tool	Maven
• Deployment	Docker & Docker Compose
