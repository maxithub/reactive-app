CREATE USER r2appuser WITH PASSWORD 'demo123';

CREATE DATABASE r2app OWNER r2appuser;

GRANT ALL PRIVILEGES ON DATABASE r2app to r2appuser;