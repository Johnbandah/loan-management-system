require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

mongoose.connect('mongodb://localhost:27017/loan_management')
  .then(() => console.log('✅ Connected to MongoDB'))
  .catch(err => console.error('❌ Connection error:', err));

// Define schemas
const userSchema = new mongoose.Schema({
  username: String,
  email: String,
  password: String,
  fullName: String,
  role: String,
  isActive: Boolean
});

const customerSchema = new mongoose.Schema({
  fullName: String,
  email: String,
  phone: String,
  username: String,
  password: String,
  nationalId: String,
  address: String,
  creditScore: Number,
  kycStatus: String,
  bankName: String,
  bankAccountNumber: String,
  bankAccountName: String,
  mobileMoneyNumber: String
});

const User = mongoose.model('User', userSchema);
const Customer = mongoose.model('Customer', customerSchema);

async function seedUsers() {
  try {
    // Delete existing
    await User.deleteMany({});
    await Customer.deleteMany({});
    console.log('🗑️  Removed existing users');

    // Create admin with correct hash
    const adminPassword = await bcrypt.hash('admin123', 10);
    console.log(`🔐 Admin hash: ${adminPassword}`);

    const admin = await User.create({
      username: 'admin',
      email: 'admin@loan.com',
      password: adminPassword,
      fullName: 'System Administrator',
      role: 'ADMIN',
      isActive: true
    });

    // Create customer with correct hash
    const customerPassword = await bcrypt.hash('password123', 10);
    console.log(`🔐 Customer hash: ${customerPassword}`);

    const customer = await Customer.create({
      fullName: 'John Doe',
      email: 'john@example.com',
      phone: '+265 999 123 456',
      username: 'johndoe',
      password: customerPassword,
      nationalId: 'MW-1234-5678',
      address: 'Lilongwe, Malawi',
      creditScore: 720,
      kycStatus: 'VERIFIED',
      bankName: 'NBM',
      bankAccountNumber: '1234567890',
      bankAccountName: 'John Doe',
      mobileMoneyNumber: '+265 999 123 456'
    });

    console.log('\n✅ Users created successfully!');
    console.log('========================================');
    console.log('👤 Admin Login:');
    console.log(`   Username: ${admin.username}`);
    console.log(`   Password: admin123`);
    console.log('');
    console.log('👤 Customer Login:');
    console.log(`   Username: ${customer.username}`);
    console.log(`   Password: password123`);
    console.log('========================================');

    // Verify passwords work
    const verifyAdmin = await User.findOne({ username: 'admin' });
    const adminMatch = await bcrypt.compare('admin123', verifyAdmin.password);
    
    const verifyCustomer = await Customer.findOne({ username: 'johndoe' });
    const customerMatch = await bcrypt.compare('password123', verifyCustomer.password);

    console.log('\n🔐 Verification:');
    console.log(`   Admin: ${adminMatch ? '✅ Working' : '❌ Failed'}`);
    console.log(`   Customer: ${customerMatch ? '✅ Working' : '❌ Failed'}`);

    process.exit(0);
  } catch (error) {
    console.error('❌ Error:', error.message);
    process.exit(1);
  }
}

seedUsers();