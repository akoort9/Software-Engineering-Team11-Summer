// Runs a simple server on http://localhost:8000
const express = require('express');
const app = express();

// host 'public' and listen
app.listen(8000, () => console.log('listening at port 8000'));
app.use(express.static('public'));

