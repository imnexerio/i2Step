
```markdown
# i2step-backend

## Overview
This project is the backend for the i2step application. It is built using Python and Flask, with SQLAlchemy for database interactions and Flask-JWT-Extended for handling JWT authentication.

## Project Structure
- `app.py`: The main application file containing all the routes and logic for handling user authentication, transactions, and orders.
- `README.md`: This file, providing an overview and setup instructions for the project.

## Key Components
- **Flask**: A micro web framework used to build the web application.
- **SQLAlchemy**: An ORM (Object Relational Mapper) used to interact with the MySQL database.
- **Flask-Login**: Used for managing user sessions.
- **Flask-JWT-Extended**: Used for handling JWT authentication.

## Models
- **User**: Represents a user in the system.
- **Transaction**: Represents a financial transaction.
- **Order**: Represents an order placed by a user.

## Routes
- `/login`: POST route for user login.
- `/logout`: GET route for user logout.
- `/username`: GET route to get the current username.
- `/gettransactions`: GET route to fetch transactions.
- `/getorders`: GET route to fetch orders.
- `/initiatetransaction`: POST route to initiate a transaction.
- `/initiateorder`: POST route to initiate an order.
- `/modifytransaction`: POST route to modify a transaction.
- `/modifyorder`: POST route to modify an order.
- `/modifytransaction_delete`: POST route to deactivate a transaction.
- `/modifyorder_delete`: POST route to deactivate an order.

## Setup Instructions

### Prerequisites
- Python 3.x
- MySQL

### Installing MySQL
1. **Download and Install MySQL**:
   - Download MySQL from the [official website](https://dev.mysql.com/downloads/).
   - Follow the installation instructions for your operating system.

2. **Create a Database**:
   - Open MySQL Workbench or use the command line.
   - Create a new database:
     ```sql
     CREATE DATABASE test3;
     ```

### Setting Up the Project
1. **Clone the Repository**:
   ```sh
   git clone https://github.com/yourusername/i2step-backend.git
   cd i2step-backend
   ```

2. **Create a Virtual Environment**:
   ```sh
   python -m venv venv
   source venv/bin/activate  # On Windows use `venv\Scripts\activate`
   ```

3. **Install Dependencies**:
   ```sh
   pip install -r requirements.txt
   ```

4. **Configure the Database**:
   - Update the `SQLALCHEMY_DATABASE_URI` in `app.py` with your MySQL credentials:
     ```python
     app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:root@localhost/your_database_name'
     ```

5. **Run the Application**:
   ```sh
   python app.py
   ```

6. **Access the Application**:
   - Open Android studio and use this url to connect app with backend to `http://127.0.0.1:5000/`.

## Notes
- Ensure to change the `SECRET_KEY` and `JWT_SECRET_KEY` in `app.py` before deploying to production.
- Passwords should be hashed in production for security.

## License
This project is licensed under the MIT License.
```

### Explanation

1. **Overview**: Provides a brief description of the project, including the technologies used.
2. **Project Structure**: Lists the main files in the project and their purposes.
3. **Key Components**: Describes the main libraries and frameworks used in the project.
4. **Models**: Lists the main data models used in the application.
5. **Routes**: Lists the API endpoints available in the application.
6. **Setup Instructions**: Provides step-by-step instructions for setting up the project, including prerequisites, installing MySQL, cloning the repository, creating a virtual environment, installing dependencies, configuring the database, running the application, and accessing it.
7. **Notes**: Includes important notes about security and configuration.
8. **License**: Specifies the license under which the project is distributed.