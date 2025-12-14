create database SleepMix

-- 1. Tabel USERS (Untuk Autentikasi)
CREATE TABLE [USER] (
    userId INT PRIMARY KEY IDENTITY(1,1), -- IDENTITY(1,1) untuk Auto-Increment
    nama NVARCHAR(100) NOT NULL, -- Menggunakan NVARCHAR untuk teks
    email NVARCHAR(255) UNIQUE NOT NULL,
    password_hash CHAR(64) NOT NULL, -- CHAR(64) jika menggunakan SHA-256
    is_logged_in INT DEFAULT 0
);

CREATE TABLE SOUND (
    soundId INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(100) UNIQUE NOT NULL,
    filePath NVARCHAR(255) NOT NULL,
    iconResource NVARCHAR(50)
);

CREATE TABLE MIX (
    mixId INT PRIMARY KEY IDENTITY(1,1),
    userId INT NOT NULL,
    mixName NVARCHAR(30) NOT NULL, -- Maksimal 30 karakter [cite: 320]
    creationDate DATETIME NOT NULL DEFAULT GETDATE(), -- Menggunakan GETDATE() untuk waktu saat ini
    
    -- Definisi Foreign Key (FK)
    FOREIGN KEY (userId) REFERENCES [USER](userId) ON DELETE CASCADE
);

CREATE TABLE MIX_SOUND (
    mixSoundId INT PRIMARY KEY IDENTITY(1,1),
    mixId INT NOT NULL,
    soundId INT NOT NULL,
    volumeLevel INT NOT NULL, -- Range 0-100% [cite: 291]
    
    -- Compound Unique Constraint: Mencegah suara yang sama dimasukkan dua kali dalam mix yang sama
    CONSTRAINT UQ_MixSound UNIQUE (mixId, soundId),

    -- Definisi Foreign Key (FK) ke MIX
    FOREIGN KEY (mixId) REFERENCES MIX(mixId) ON DELETE CASCADE,
    
    -- Definisi Foreign Key (FK) ke SOUND
    FOREIGN KEY (soundId) REFERENCES SOUND(soundId) ON DELETE CASCADE
);