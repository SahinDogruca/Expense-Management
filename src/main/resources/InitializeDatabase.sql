-- 1. Unit (Birim) verileri - Farklı bütçe limitleriyle
INSERT INTO unit (name, budget_limit, threshold_limit) VALUES
                                                           ('IT Departmanı', 50000.00, 5000.00),  -- Yüksek bütçeli birim
                                                           ('Muhasebe', 20000.00, 2000.00),       -- Orta bütçeli birim
                                                           ('İnsan Kaynakları', 10000.00, 1000.00), -- Düşük bütçeli birim
                                                           ('Satış', 30000.00, 0.00);             -- Eşik limiti olmayan birim

-- 2. Employee (Çalışan) verileri - Hiyerarşik yapıyla
INSERT INTO employee (name, password, unit_id, manager_id) VALUES
-- IT Departmanı
('Ahmet Yılmaz', 'Ahmet123', 1, NULL),  -- IT Müdürü (üst yönetici)
('Ayşe Kaya', 'Ayşe123', 1, 1),        -- IT Takım Lideri
('Mehmet Demir', 'Mehmet123', 1, 2),     -- IT Çalışanı
('Zeynep Şahin', 'Zeynep123', 1, 2),     -- IT Çalışanı (edge case: aynı yöneticiye bağlı)
('Ali Vural', 'Ali123', 1, NULL),     -- IT'de yöneticisi olmayan çalışan

-- Muhasebe
('Fatma Öztürk', 'Fatma123', 2, NULL),  -- Muhasebe Müdürü
('Mustafa Arslan', 'Mustafa123', 2, 6),   -- Muhasebe Çalışanı

-- İnsan Kaynakları
('Elif Koç', 'Elif123', 3, NULL),      -- IK Müdürü
('Canan Erdem', 'Canan123', 3, 8),      -- IK Uzmanı

-- Satış
('Burak Güneş', 'Burak123', 4, NULL),  -- Satış Müdürü
('Deniz Yıldız', 'Deniz123', 4, 10);   -- Satış Temsilcisi

-- 3. Expense Category (Harcama Kategorileri)
INSERT INTO expense_category (name) VALUES
                                        ('Yol ve Konaklama'),      -- 1
                                        ('Yemek'),                 -- 2
                                        ('Ofis Malzemeleri'),      -- 3
                                        ('Eğitim'),                -- 4
                                        ('Teknoloji Harcamaları'), -- 5
                                        ('Diğer');                 -- 6 (Edge case: genel kategori)

-- 4. Expense Request (Harcama Talepleri) - Farklı durumlar ve edge case'ler
INSERT INTO expense_request (employee_id, category_id, amount, status, created_at, updated_at) VALUES
-- IT Departmanı Talepleri
(2, 1, 1200.00, 'APPROVED', '2023-01-10 09:15:22+03', '2023-01-11 14:30:00+03'),  -- Onaylanmış konferans seyahati
(3, 5, 3500.00, 'REJECTED', '2023-01-15 11:20:33+03', '2023-01-16 10:15:00+03'),  -- Reddedilmiş yazılım lisansı
(4, 2, 150.00, 'PENDING', '2023-02-01 13:45:12+03', NULL),                        -- Beklemede olan ekip yemeği
(2, 3, 450.00, 'APPROVED', '2023-02-05 10:30:00+03', '2023-02-05 16:45:00+03'),  -- Ofis malzemeleri
(3, 1, 1800.00, 'CANCELLED', '2023-02-10 08:20:15+03', '2023-02-10 09:30:00+03'), -- İptal edilmiş seyahat
(5, 5, 12000.00, 'PENDING', '2023-02-15 16:40:00+03', NULL),                      -- Bütçe limitini aşan büyük talep (edge case)
(5, 6, 75.50, 'APPROVED', '2023-02-20 14:10:33+03', '2023-02-20 15:00:00+03'),    -- Diğer kategoride küçük talep

-- Muhasebe Talepleri
(6, 4, 800.00, 'APPROVED', '2023-01-20 12:30:00+03', '2023-01-21 09:15:00+03'),   -- Muhasebe eğitimi
(7, 2, 200.00, 'REJECTED', '2023-02-01 18:20:00+03', '2023-02-02 10:00:00+03'),   -- Reddedilmiş yemek

-- İK Talepleri
(8, 1, 950.00, 'APPROVED', '2023-01-25 10:15:00+03', '2023-01-25 14:30:00+03'),   -- İş görüşmesi seyahati
(9, 3, 120.00, 'APPROVED', '2023-02-05 11:45:00+03', '2023-02-05 15:20:00+03'),    -- Ofis malzemeleri

-- Satış Talepleri
(10, 1, 2500.00, 'PENDING', '2023-02-12 09:30:00+03', NULL),                      -- Müşteri ziyareti seyahati
(11, 2, 300.00, 'APPROVED', '2023-02-14 12:00:00+03', '2023-02-14 14:00:00+03');  -- Müşteri yemeği

-- 5. Reimbursement (Geri Ödemeler) - Onaylanmış talepler için
INSERT INTO reimbursement (expense_id, reimbursed_amount, reimbursement_date) VALUES
                                                                                  (1, 1200.00, '2023-01-20'),  -- Tam geri ödeme
                                                                                  (4, 450.00, '2023-02-10'),   -- Tam geri ödeme
                                                                                  (7, 75.50, '2023-02-25'),    -- Tam geri ödeme
                                                                                  (8, 800.00, '2023-01-25'),   -- Tam geri ödeme
                                                                                  (10, 950.00, '2023-01-30'),  -- Tam geri ödeme
                                                                                  (11, 120.00, '2023-02-10'),  -- Tam geri ödeme
                                                                                  (13, 250.00, '2023-02-20');  -- Kısmi geri ödeme (edge case: tamamı değil)