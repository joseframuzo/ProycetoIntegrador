require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

app.use('/api/auth', require('./routes/auth'));
app.use('/api/professionals', require('./routes/professionals'));

app.get('/', (_, res) => res.send('API OK'));

const PORT = process.env.PORT || 4000;
app.listen(PORT, '0.0.0.0', () => console.log(`API arriba  :${PORT}`));