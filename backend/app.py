from flask import Flask, request, jsonify, make_response
from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from datetime import datetime, timedelta
from sqlalchemy.orm import joinedload
from sqlalchemy.exc import SQLAlchemyError

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:root@localhost/your_database'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = 'your_secret_key'
app.config['JWT_SECRET_KEY'] = 'your_jwt_secret_key'  # Change this in production

db = SQLAlchemy(app)
login_manager = LoginManager(app)
login_manager.login_view = 'login'
jwt = JWTManager(app)

class User(UserMixin, db.Model):
    username = db.Column(db.String(80), primary_key=True)
    password = db.Column(db.String(120), nullable=False)
    role = db.Column(db.String(20), nullable=False)
    name = db.Column(db.String(80))
    address = db.Column(db.String(80))
    phone_no = db.Column(db.BigInteger, nullable=False)

    def get_id(self):
        return self.username

    def __repr__(self):
        return f'<User {self.username}>'
    

class Transaction(db.Model):
    transaction_id = db.Column(db.String(80), primary_key=True)
    payment_method = db.Column(db.String(50), nullable=False)
    amount = db.Column(db.Float, nullable=False)
    status = db.Column(db.String(20), nullable=False, default='INITIATED')
    initiated_date = db.Column(db.DateTime, nullable=False, default=datetime.now)
    verified_date = db.Column(db.DateTime)
    initiated_by_id = db.Column(db.String(80), db.ForeignKey('user.username'), nullable=False)
    verified_by_id = db.Column(db.String(80), db.ForeignKey('user.username'))
    initiated_for = db.Column(db.String(80), db.ForeignKey('user.username'), nullable=False)
    record_status = db.Column(db.Integer, nullable=False, default=1)
    record_status_modified_by = db.Column(db.String(80), db.ForeignKey('user.username'))
    total_amount = db.Column(db.Float)  # New column
    comments = db.Column(db.String(80))
    initiated_by = db.relationship('User', foreign_keys=[initiated_by_id], backref='initiated_transactions')
    verified_by = db.relationship('User', foreign_keys=[verified_by_id], backref='verified_transactions')
    for_user = db.relationship('User', foreign_keys=[initiated_for], backref='for_transactions')
    modified_by = db.relationship('User', foreign_keys=[record_status_modified_by], backref='modified_transactions')


class Order(db.Model):
    __tablename__ = 'orders'
    order_id = db.Column(db.String(80), primary_key=True)
    no_bags = db.Column(db.Integer, nullable=False)
    rate = db.Column(db.Float, nullable=False)
    vehicle_no = db.Column(db.String(50))
    status = db.Column(db.String(20), nullable=False, default='INITIATED')
    initiated_date = db.Column(db.DateTime, nullable=False, default=datetime.now)
    verified_date = db.Column(db.DateTime)
    initiated_by_id = db.Column(db.String(50), db.ForeignKey('user.username'), nullable=False)
    verified_by_id = db.Column(db.String(50), db.ForeignKey('user.username'))
    initiated_for = db.Column(db.String(50), db.ForeignKey('user.username'), nullable=False)
    record_status = db.Column(db.Integer, nullable=False, default=1)
    record_status_modified_by = db.Column(db.String(80), db.ForeignKey('user.username'))
    comments = db.Column(db.String(80))
    initiated_by = db.relationship('User', foreign_keys=[initiated_by_id], backref='initiated_orders')
    verified_by = db.relationship('User', foreign_keys=[verified_by_id], backref='verified_orders')
    for_user = db.relationship('User', foreign_keys=[initiated_for], backref='for_orders')
    modified_by = db.relationship('User', foreign_keys=[record_status_modified_by], backref='modified_orders')
    def generate_order_id(self):
        return f"{self.initiated_for}_{datetime.now().strftime('%Y%m%d%H%M%S')}"


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    print(data)
    user = User.query.filter_by(username=data['username']).first()
    if user and user.password == data['password']:  # Hash passwords in production!
        login_user(user)
        access_token = create_access_token(identity={'username': user.username, 'role': user.role},expires_delta=timedelta(minutes=5))
        # Returning username and role along with access token
        # print(user.username, user.role, access_token)
        return jsonify(access_token=access_token,name=user.name, username=user.username, role=user.role)
    else:
        return jsonify({"message": "Invalid username or password!"}), 401

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return jsonify({"message": "Logged out successfully!"})

@app.route('/username', methods=['GET'])
@jwt_required()
def get_username():
    current_user = get_jwt_identity()
    return jsonify(current_user)


@app.route('/gettransactions', methods=['GET'])
@jwt_required()
def get_transactions():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'M']:
        transactions = Transaction.query.filter_by(record_status=1).options(joinedload(Transaction.initiated_by), joinedload(Transaction.verified_by)).order_by(Transaction.initiated_date.desc()).all()
    else:
        transactions = Transaction.query.filter_by(initiated_for=current_user['username'], record_status=1).options(joinedload(Transaction.initiated_by), joinedload(Transaction.verified_by)).order_by(Transaction.initiated_date.desc()).all()

        # print(transactions)
    users = {user.username: user for user in User.query.all()}
    # print("gettransactions = ",transactions)
    
    result = []
    for t in transactions:
        initiated_by_user = users.get(t.initiated_by_id)
        verified_by_user = users.get(t.verified_by_id)
        initiated_for_user = users.get(t.initiated_for)
        
        result.append({
            'transaction_id': t.transaction_id,
            'payment_method': t.payment_method,
            'amount': t.amount,
            'status': t.status,
            'initiated_date': t.initiated_date,
            'verified_date': t.verified_date if t.verified_date else "NA",
            'initiated_by': initiated_by_user.username if initiated_by_user else "NA",
            'verified_by': verified_by_user.username if verified_by_user else "NA",
            'initiated_for': initiated_for_user.username if initiated_for_user else "NA",
            'total_amount': t.total_amount,
            'comments': t.comments
        })
    # print(result)
    return jsonify(result)

@app.route('/getorders', methods=['GET'])
@jwt_required()
def get_orders():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'M']:
        orders = Order.query.filter_by(record_status=1).options(joinedload(Order.initiated_by)).order_by(Order.initiated_date.desc()).all()
    else:
        orders = Order.query.filter_by(initiated_for=current_user['username'], record_status=1).options(joinedload(Order.initiated_by)).order_by(Order.initiated_date.desc()).all()

    users = {user.username: user for user in User.query.all()}
    
    result = []
    for o in orders:
        initiated_by_user = users.get(o.initiated_by_id)
        initiated_for_user = users.get(o.initiated_for)
        verified_by_user = users.get(o.verified_by_id)
        
        result.append({
            'order_id': o.order_id,
            'no_bags': o.no_bags,
            'rate': o.rate,
            'vehicle_no': o.vehicle_no if o.vehicle_no else "NA",
            'status': o.status,
            'initiated_date': o.initiated_date,
            'initiated_by': initiated_by_user.username if initiated_by_user else "NA",
            'verified_by': verified_by_user.username if verified_by_user else "NA",
            'initiated_for': initiated_for_user.username if initiated_for_user else "NA",
            'comments':o.comments,
        })
    return jsonify(result)

@app.route('/initiatetransaction', methods=['POST'])
@jwt_required()
def initiate_transaction():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'M']:
        data = request.json
        print(data)
        transaction_id = data.get('transaction_id')
        payment_method = data.get('payment_method')
        amount = data.get('amount')
        initiated_for = data.get('initiated_for')
        comments = data.get('comments')
        
        # Validate required fields
        if not all([transaction_id,payment_method, amount, initiated_for]):
            # print("missing required fields")
            return jsonify({'error': 'Missing required fields'}), 400
        
        # Fetch and validate the transaction to be modified
        transaction = Transaction.query.filter_by(initiated_for=initiated_for).first()
        #check if it is latest record of the user
        latest_transaction = Transaction.query.filter_by(initiated_for=initiated_for).order_by(Transaction.initiated_date.desc()).first()
        if latest_transaction:
            if latest_transaction.record_status==1:
                if latest_transaction.status!='VERIFIED':
                    return jsonify({'error': 'Already a pending transaction'}), 400


        # Create the transaction
        transaction = Transaction(
            transaction_id=transaction_id,
            payment_method=payment_method,
            amount=-(amount),
            initiated_for=initiated_for,
            initiated_by_id=current_user['username'],  # Assuming the username is stored in JWT identity
            comments = comments
        )
        
        # Attempt to save the transaction to the database
        try:
            # print("transaction initiated",transaction)
            db.session.add(transaction)
            db.session.commit()
            return jsonify({'message': 'Transaction initiated successfully', 'transaction': transaction_id}), 201  # Assuming a to_dict method on the Transaction model
        except SQLAlchemyError as e:
            # print("error",e)
            db.session.rollback()
            return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        # print("unauthorized")
        return jsonify({'error': 'Unauthorized'}), 403


@app.route('/initiateorder', methods=['POST'])
@jwt_required()
def initiate_order():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'M']:
        data = request.json
        print(data)
        transaction_id=data.get('transaction_id')
        no_bags = data.get('no_bags')
        rate = data.get('rate')
        vehicle_no = data.get('vehicle_no')
        initiated_for = data.get('initiated_for')
        payment_method=data.get('payment_method')
        comments = data.get('comments')
        
        # Validate required fields
        if not all([transaction_id,payment_method, no_bags,rate, initiated_for,initiated_for,comments]):
            # print("missing required fields")
            return jsonify({'error': 'Missing required fields'}), 400
        
        # Fetch and validate the transaction to be modified
        transaction = Transaction.query.filter_by(initiated_for=initiated_for).first()
        #check if it is latest record of the user
        latest_transaction = Transaction.query.filter_by(initiated_for=initiated_for).order_by(Transaction.initiated_date.desc()).first()
        if latest_transaction:
            if latest_transaction.record_status==1:
                if latest_transaction.status!='VERIFIED':
                    return jsonify({'error': 'Already a pending transaction'}), 400

        
        # Create the order
        order = Order(
            order_id=transaction_id,
            no_bags=no_bags,
            rate=rate,
            vehicle_no=vehicle_no,
            initiated_for=initiated_for,
            initiated_by_id=current_user['username'],  # Assuming the username is stored in JWT identity
            comments=comments
        )

        transaction=Transaction(
            transaction_id=transaction_id,
            payment_method=payment_method,
            amount=rate*no_bags,
            initiated_for=initiated_for,
            initiated_by_id=current_user['username'],  # Assuming the username is stored in JWT identity
            comments = comments
        )
        
        
        # Attempt to save the order to the database
        try:
            # print("order initiated", order)
            db.session.add(order)
            db.session.add(transaction)
            db.session.commit()
            return jsonify({'message': 'Order initiated successfully', 'order_id': order.order_id}), 201  # Assuming a to_dict method on the Order model
        except SQLAlchemyError as e:
            # print("error", e)
            db.session.rollback()
            return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        # print("unauthorized")
        return jsonify({'error': 'Unauthorized'}), 403
    
@app.route('/modifytransaction', methods=['POST'])
@jwt_required()
def modify_transaction():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'U']:
        data = request.json
        # print("data received",data)
        status = data.get('status')
        transaction_id = data.get('transaction_id')

        # Validate required fields and status must be 'verified'
        if status != 'VERIFIED':
            # print("status not verified")
            return jsonify({'error': 'Missing required fields or invalid status'}), 400

        # Fetch and validate the transaction to be modified
        transaction = Transaction.query.filter_by(transaction_id=transaction_id).first()
        if not transaction:
            # print("transaction not found")
            return jsonify({'error': 'Transaction not found'}), 404
        print(transaction.initiated_for)
        # Get the latest transaction with the same initiated_for user
        # latest_transaction = Transaction.query.filter_by(initiated_for=transaction.initiated_for).order_by(Transaction.initiated_date.desc()).first()

        # Fetch all transactions ordered by initiated_date in descending order
        transactions = Transaction.query.filter_by(initiated_for=transaction.initiated_for).order_by(Transaction.initiated_date.desc()).all()

        # Check if there are at least two transactions
        if len(transactions) >= 2:
            # Select the second to last transaction
            latest_transaction = transactions[1]
        else:
            # Handle the case where there are not enough transactions
            latest_transaction = None  # Or any other fallback logic

        if latest_transaction:
            latest_total_amount = latest_transaction.total_amount or 0
            
        else:
            latest_total_amount = 0
        print(latest_total_amount)
        # Update the transaction
        transaction.status = 'VERIFIED'
        transaction.verified_date = datetime.now()
        transaction.verified_by_id = current_user['username']  # Assuming the username is stored in JWT identity

        if transaction.record_status == 1:
            # Add the amount to the total_amount
            transaction.total_amount = latest_total_amount + transaction.amount
        else:
            # Set the total_amount to the latest total_amount without adding the amount
            transaction.total_amount = latest_total_amount

        try:
            # print("transaction sucess",transaction)
            db.session.commit()
            return jsonify({'message': 'Transaction modified successfully', 'transaction': transaction_id}), 200  # Assuming a to_dict method on the Transaction model
        except SQLAlchemyError as e:
            db.session.rollback()
            return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        return jsonify({'error': 'Unauthorized'}), 403
    
@app.route('/modifyorder', methods=['POST'])
@jwt_required()
def modify_order():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A', 'U']:
        data = request.json
        # print("data received", data)
        status = data.get('status')
        order_id = data.get('order_id')

        # Validate required fields and status must be 'verified'
        if status != 'VERIFIED':
            # print("status not verified")
            return jsonify({'error': 'Missing required fields or invalid status'}), 400

        # Fetch and validate the order to be modified
        order = Order.query.filter_by(order_id=order_id).first()
        if not order:
            # print("order not found")
            return jsonify({'error': 'Order not found'}), 404

        else:
            # Update the order
            order.status = 'VERIFIED'
            order.verified_date = datetime.now()
            order.verified_by_id = current_user['username']  # Assuming the username is stored in JWT identity

            try:
                # print("order success", order)
                db.session.commit()
                return jsonify({'message': 'Order modified successfully', 'order': order_id}), 200
            except SQLAlchemyError as e:
                db.session.rollback()
                return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        return jsonify({'error': 'Unauthorized'}), 403

@app.route('/modifytransaction_delete', methods=['POST'])
@jwt_required()
def modify_transaction_delete():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A']:
        data = request.json
        # print("data received", data)
        transaction_id = data.get('transaction_id')

        # Validate transaction_id is provided
        if not transaction_id:
            # print("transaction_id not provided")
            return jsonify({'error': 'Missing required field: transaction_id'}), 400

        # Fetch and validate the transaction to be modified
        transaction = Transaction.query.filter_by(transaction_id=transaction_id).first()

        #check if it is latest record of the user
        latest_transaction = Transaction.query.filter_by(initiated_for=transaction.initiated_for).order_by(Transaction.initiated_date.desc()).first()
        if transaction_id!=latest_transaction.transaction_id:
            return jsonify({'error': 'This transaction can\t be modified: transaction_id'}), 400

        if not transaction:
            # print("transaction not found")
            return jsonify({'error': 'Transaction not found'}), 404
        
        if transaction.status == 'VERIFIED':
            latest_total_amount = transaction.total_amount - transaction.amount
        else:
            latest_transaction = Transaction.query.filter_by(initiated_for=transaction.initiated_for).order_by(Transaction.initiated_date.desc()).all()
            # Check if there are at least two transactions
            if len(latest_transaction) >= 2:
                # Select the second to last transaction
                latest_transaction = latest_transaction[1]
            else:
                # Handle the case where there are not enough transactions
                latest_transaction = None  # Or any other fallback logic
            
            if latest_transaction:
                latest_total_amount = latest_transaction.total_amount or 0
                
            else:
                latest_total_amount = 0

        transaction.total_amount=latest_total_amount
        record_status_modified_by=current_user['username']
        transaction.record_status_modified_by=record_status_modified_by
        transaction.record_status = 0

        try:
            # print("transaction deactivation success", transaction)
            db.session.commit()
            return jsonify({'message': 'Transaction deactivated successfully', 'transaction': transaction_id}), 200
        except SQLAlchemyError as e:
            db.session.rollback()
            return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        return jsonify({'error': 'Unauthorized'}), 403
    

@app.route('/modifyorder_delete', methods=['POST'])
@jwt_required()
def modify_order_delete():
    current_user = get_jwt_identity()
    if current_user['role'] in ['A']:  # Assuming only 'A' role can delete
        data = request.json
        # print("data received", data)
        order_id = data.get('order_id')

        # Validate order_id is provided
        if not order_id:
            # print("order_id not provided")
            return jsonify({'error': 'Missing required field: order_id'}), 400

        # Fetch and validate the order to be modified
        order = Order.query.filter_by(order_id=order_id).first()
        if not order:
            # print("order not found")
            return jsonify({'error': 'Order not found'}), 404

        # Update the order to set record_status to 'DEACTIVE'
        else:
            record_status_modified_by=current_user['username']
            order.record_status_modified_by=record_status_modified_by
            order.record_status = 0
            try:
                # print("order deactivation success", order)
                db.session.commit()
                return jsonify({'message': 'Order deactivated successfully', 'order': order_id}), 200
            except SQLAlchemyError as e:
                db.session.rollback()
                return jsonify({'error': 'Database error', 'message': str(e)}), 500
    else:
        return jsonify({'error': 'Unauthorized'}), 403
    

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True)
