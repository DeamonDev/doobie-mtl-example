#! /bin/bash

sudo su postgres <<EOF
psql -c 'CREATE DATABASE manifolds;'
EOF

su -c "psql -d manifolds -c \"CREATE TABLE IF NOT EXISTS algebraic_varieties (id INT PRIMARY KEY NOT NULL, name VARCHAR(50) UNIQUE NOT NULL,
equation VARCHAR(100) UNIQUE NOT NULL, euler_char INT); SELECT * FROM algebraic_varieties;\"" postgres
