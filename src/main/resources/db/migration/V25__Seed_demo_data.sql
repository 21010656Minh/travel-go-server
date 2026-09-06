-- V25__Seed_demo_data.sql
-- Initial demo seed data for the Travel Social Network.
--
-- Design notes:
--  * All inserts use ON CONFLICT DO NOTHING (or WHERE NOT EXISTS) so the
--    migration is safe to re-run on databases that already have data.
--  * Password for every seeded user is "Password@123" (BCrypt strength 10).
--    Hashes below were generated externally and validated against Spring
--    Security's BCryptPasswordEncoder.
--  * Media URLs are public placeholder hosts (picsum.photos, pravatar.cc).
--    They will resolve to random images; replace them later via the
--    upload endpoints or by editing the URLs in this file.
--  * Cross-table references use deterministic UUIDs derived from
--    md5('seed:' || <logical_name>) so the seed is reproducible.

-- =========================================================================
-- 1. USERS
-- =========================================================================
-- 12 users: 1 admin + 11 regular. user_name is the unique login handle.
-- Avatar: pravatar.cc index picked per gender so the picture matches the
-- user_profile.gender column. Cover: travel-themed Unsplash photos keyed
-- to the user's bio/location.
INSERT INTO users (user_id, user_name, email, role, avatar_img, cover_img, status, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-000000000001', 'admin',           'admin@travelvn.local',          'ADMIN', 'https://i.pravatar.cc/300?img=15', 'https://images.unsplash.com/photo-1508433957232-3107f5fd5995?w=1200&h=400&fit=crop',     'ACTIVE', NOW() - INTERVAL '120 days', NOW()),
    ('11111111-1111-1111-1111-000000000002', 'an_nguyen',       'an.nguyen@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=12', 'https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?w=1200&h=400&fit=crop',  'ACTIVE', NOW() - INTERVAL '90 days',  NOW()),
    ('11111111-1111-1111-1111-000000000003', 'binh_tran',       'binh.tran@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=5',  'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=1200&h=400&fit=crop',  'ACTIVE', NOW() - INTERVAL '88 days',  NOW()),
    ('11111111-1111-1111-1111-000000000004', 'cuong_le',        'cuong.le@travelvn.local',       'USER',  'https://i.pravatar.cc/300?img=33', 'https://images.unsplash.com/photo-1528909514045-2fa4ac7a08ba?w=1200&h=400&fit=crop',  'ACTIVE', NOW() - INTERVAL '85 days',  NOW()),
    ('11111111-1111-1111-1111-000000000005', 'dao_pham',        'dao.pham@travelvn.local',       'USER',  'https://i.pravatar.cc/300?img=16', 'https://images.unsplash.com/photo-1542640244-7e672d6cef4e?w=1200&h=400&fit=crop',   'ACTIVE', NOW() - INTERVAL '80 days',  NOW()),
    ('11111111-1111-1111-1111-000000000006', 'em_hoang',        'em.hoang@travelvn.local',       'USER',  'https://i.pravatar.cc/300?img=51', 'https://images.unsplash.com/photo-1480497490787-505ec076689f?w=1200&h=400&fit=crop','ACTIVE', NOW() - INTERVAL '75 days',  NOW()),
    ('11111111-1111-1111-1111-000000000007', 'phuong_vo',       'phuong.vo@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=20', 'https://images.unsplash.com/photo-1528127269322-539801943592?w=1200&h=400&fit=crop','ACTIVE', NOW() - INTERVAL '70 days',  NOW()),
    ('11111111-1111-1111-1111-000000000008', 'giang_bui',       'giang.bui@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=56', 'https://images.unsplash.com/photo-1582967788606-a171c1080cb0?w=1200&h=400&fit=crop','ACTIVE', NOW() - INTERVAL '65 days',  NOW()),
    ('11111111-1111-1111-1111-000000000009', 'hieu_doan',       'hieu.doan@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=60', 'https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=1200&h=400&fit=crop',  'ACTIVE', NOW() - INTERVAL '60 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000a', 'mai_duong',       'mai.duong@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=24', 'https://images.unsplash.com/photo-1500964757637-c85e8a162699?w=1200&h=400&fit=crop',  'ACTIVE', NOW() - INTERVAL '55 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000b', 'ngoc_ly',         'ngoc.ly@travelvn.local',        'USER',  'https://i.pravatar.cc/300?img=44', 'https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=1200&h=400&fit=crop',   'ACTIVE', NOW() - INTERVAL '50 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000c', 'khanh_ngo',       'khanh.ngo@travelvn.local',      'USER',  'https://i.pravatar.cc/300?img=47', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=1200&h=400&fit=crop',   'ACTIVE', NOW() - INTERVAL '45 days',  NOW())
ON CONFLICT (user_id) DO NOTHING;

-- =========================================================================
-- 2. USER PROFILES
-- =========================================================================
INSERT INTO user_profiles (user_id, full_name, location, gender, dob, about, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-000000000001', 'Quản trị viên',          'Hà Nội',         'OTHER',  '1990-01-15', 'Quản trị hệ thống Travel Social Network.',                NOW() - INTERVAL '120 days', NOW()),
    ('11111111-1111-1111-1111-000000000002', 'Nguyễn Văn An',          'Hà Nội',         'MALE',   '1995-03-22', 'Phượt thủ Bắc-Nam, mê trekking Tây Bắc.',                  NOW() - INTERVAL '90 days',  NOW()),
    ('11111111-1111-1111-1111-000000000003', 'Trần Thị Bình',         'TP. Hồ Chí Minh','FEMALE', '1996-07-08', 'Food blogger, đi đâu ăn đó.',                              NOW() - INTERVAL '88 days',  NOW()),
    ('11111111-1111-1111-1111-000000000004', 'Lê Hùng Cường',         'Đà Nẵng',        'MALE',   '1992-11-30', 'Biển và núi là cuộc sống.',                                NOW() - INTERVAL '85 days',  NOW()),
    ('11111111-1111-1111-1111-000000000005', 'Phạm Thị Dao',          'Huế',            'FEMALE', '1998-05-14', 'Yêu cố đô và lịch sử.',                                   NOW() - INTERVAL '80 days',  NOW()),
    ('11111111-1111-1111-1111-000000000006', 'Hoàng Minh Em',         'Hải Phòng',      'MALE',   '1991-09-03', 'Kỹ sư phần mềm, đam mê chụp ảnh phong cảnh.',               NOW() - INTERVAL '75 days',  NOW()),
    ('11111111-1111-1111-1111-000000000007', 'Võ Thị Phương',         'Cần Thơ',        'FEMALE', '1994-12-19', 'Miền Tây sông nước, chuyên viết về chợ nổi.',              NOW() - INTERVAL '70 days',  NOW()),
    ('11111111-1111-1111-1111-000000000008', 'Bùi Văn Giang',         'Nha Trang',      'MALE',   '1990-04-25', 'Lặn biển và khám phá san hô.',                             NOW() - INTERVAL '65 days',  NOW()),
    ('11111111-1111-1111-1111-000000000009', 'Đoàn Trung Hiếu',       'Sa Pa',          'MALE',   '1988-08-17', 'Hướng dẫn viên địa phương.',                                NOW() - INTERVAL '60 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000a', 'Dương Thị Mai',         'Hà Nội',         'FEMALE', '1997-02-11', 'Nhiếp ảnh gia du lịch, mê mây Tây Bắc.',                  NOW() - INTERVAL '55 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000b', 'Lý Hồng Ngọc',          'Đà Lạt',         'FEMALE', '1993-10-09', 'Sống chậm giữa thành phố ngàn hoa.',                       NOW() - INTERVAL '50 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000c', 'Ngô Khánh Linh',        'Phú Quốc',       'OTHER',  '1996-06-27', 'Digital nomad, làm việc từ biển.',                          NOW() - INTERVAL '45 days',  NOW())
ON CONFLICT (user_id) DO NOTHING;

-- =========================================================================
-- 3. USER CREDENTIALS (passwords)
-- =========================================================================
-- Both bcrypt hashes resolve to the plaintext "Password@123"
-- user_credentials has UNIQUE (provider, provider_user_id). When all rows
-- have provider_user_id NULL, Postgres treats NULLs as distinct, so ON
-- CONFLICT on those columns never matches. Use explicit credential_ids
-- and ON CONFLICT on the PK instead.
INSERT INTO user_credentials (credential_id, user_id, provider, provider_user_id, password, created_at) VALUES
    ('11111111-cccc-cccc-cccc-000000000001', '11111111-1111-1111-1111-000000000001', 'LOCAL', NULL, '$2b$10$geR23fGyqUlsMdqmogv7y.iQRkaiux1hkQol1h0rdPsGKXHaLu2di', NOW() - INTERVAL '120 days'),
    ('11111111-cccc-cccc-cccc-000000000002', '11111111-1111-1111-1111-000000000002', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '90 days'),
    ('11111111-cccc-cccc-cccc-000000000003', '11111111-1111-1111-1111-000000000003', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '88 days'),
    ('11111111-cccc-cccc-cccc-000000000004', '11111111-1111-1111-1111-000000000004', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '85 days'),
    ('11111111-cccc-cccc-cccc-000000000005', '11111111-1111-1111-1111-000000000005', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '80 days'),
    ('11111111-cccc-cccc-cccc-000000000006', '11111111-1111-1111-1111-000000000006', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '75 days'),
    ('11111111-cccc-cccc-cccc-000000000007', '11111111-1111-1111-1111-000000000007', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '70 days'),
    ('11111111-cccc-cccc-cccc-000000000008', '11111111-1111-1111-1111-000000000008', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '65 days'),
    ('11111111-cccc-cccc-cccc-000000000009', '11111111-1111-1111-1111-000000000009', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '60 days'),
    ('11111111-cccc-cccc-cccc-00000000000a', '11111111-1111-1111-1111-00000000000a', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '55 days'),
    ('11111111-cccc-cccc-cccc-00000000000b', '11111111-1111-1111-1111-00000000000b', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '50 days'),
    ('11111111-cccc-cccc-cccc-00000000000c', '11111111-1111-1111-1111-00000000000c', 'LOCAL', NULL, '$2b$10$zHBgEC/JIe9SCQD.1SBC4e1bIVlI7mA/wvsWwZHooJp1vjh2oaf8e', NOW() - INTERVAL '45 days')
ON CONFLICT (credential_id) DO NOTHING;

-- =========================================================================
-- 4. TAGS
-- =========================================================================
INSERT INTO tags (title, slug, created_at) VALUES
    ('Du lịch',         'du-lich',         NOW() - INTERVAL '90 days'),
    ('Biển đảo',        'bien-dao',        NOW() - INTERVAL '88 days'),
    ('Núi rừng',        'nui-rung',        NOW() - INTERVAL '85 days'),
    ('Ẩm thực',         'am-thuc',         NOW() - INTERVAL '82 days'),
    ('Phượt',           'phuot',           NOW() - INTERVAL '80 days'),
    ('Check-in',        'check-in',        NOW() - INTERVAL '78 days'),
    ('Cắm trại',        'cam-trai',        NOW() - INTERVAL '76 days'),
    ('Văn hóa',         'van-hoa',         NOW() - INTERVAL '74 days'),
    ('Lịch sử',         'lich-su',         NOW() - INTERVAL '72 days'),
    ('Nhiếp ảnh',       'nhiep-anh',       NOW() - INTERVAL '70 days'),
    ('Backpacking',     'backpacking',     NOW() - INTERVAL '68 days'),
    ('Resort',          'resort',          NOW() - INTERVAL '66 days'),
    ('Homestay',        'homestay',        NOW() - INTERVAL '64 days'),
    ('Mạo hiểm',        'mao-hiem',        NOW() - INTERVAL '62 days'),
    ('Gia đình',        'gia-dinh',        NOW() - INTERVAL '60 days'),
    ('Cặp đôi',         'cap-doi',         NOW() - INTERVAL '58 days'),
    ('Mùa thu',         'mua-thu',         NOW() - INTERVAL '56 days'),
    ('Mùa đông',        'mua-dong',        NOW() - INTERVAL '54 days'),
    ('Mùa xuân',        'mua-xuan',        NOW() - INTERVAL '52 days'),
    ('Mùa hè',          'mua-he',          NOW() - INTERVAL '50 days'),
    ('Đông-Tây Bắc',    'dong-tay-bac',    NOW() - INTERVAL '48 days'),
    ('Miền Tây',        'mien-tay',        NOW() - INTERVAL '46 days'),
    ('Tây Nguyên',      'tay-nguyen',      NOW() - INTERVAL '44 days'),
    ('Miền Trung',      'mien-trung',      NOW() - INTERVAL '42 days'),
    ('Hà Nội',          'ha-noi',          NOW() - INTERVAL '40 days')
ON CONFLICT (slug) DO NOTHING;

-- =========================================================================
-- 5. FRIENDSHIPS
-- =========================================================================
-- Mostly ACCEPTED, one PENDING to demonstrate the request flow.
INSERT INTO friendships (friendship_id, requester_id, receiver_id, status, created_at) VALUES
    ('22222222-2222-2222-2222-000000000001', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000003', 'ACCEPTED', NOW() - INTERVAL '80 days'),
    ('22222222-2222-2222-2222-000000000002', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000004', 'ACCEPTED', NOW() - INTERVAL '78 days'),
    ('22222222-2222-2222-2222-000000000003', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000005', 'ACCEPTED', NOW() - INTERVAL '75 days'),
    ('22222222-2222-2222-2222-000000000004', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000006', 'ACCEPTED', NOW() - INTERVAL '72 days'),
    ('22222222-2222-2222-2222-000000000005', '11111111-1111-1111-1111-000000000003', '11111111-1111-1111-1111-000000000004', 'ACCEPTED', NOW() - INTERVAL '70 days'),
    ('22222222-2222-2222-2222-000000000006', '11111111-1111-1111-1111-000000000003', '11111111-1111-1111-1111-000000000007', 'ACCEPTED', NOW() - INTERVAL '68 days'),
    ('22222222-2222-2222-2222-000000000007', '11111111-1111-1111-1111-000000000004', '11111111-1111-1111-1111-000000000008', 'ACCEPTED', NOW() - INTERVAL '65 days'),
    ('22222222-2222-2222-2222-000000000008', '11111111-1111-1111-1111-000000000005', '11111111-1111-1111-1111-000000000009', 'ACCEPTED', NOW() - INTERVAL '60 days'),
    ('22222222-2222-2222-2222-000000000009', '11111111-1111-1111-1111-000000000005', '11111111-1111-1111-1111-00000000000a', 'ACCEPTED', NOW() - INTERVAL '55 days'),
    ('22222222-2222-2222-2222-00000000000a', '11111111-1111-1111-1111-000000000006', '11111111-1111-1111-1111-000000000008', 'ACCEPTED', NOW() - INTERVAL '50 days'),
    ('22222222-2222-2222-2222-00000000000b', '11111111-1111-1111-1111-000000000007', '11111111-1111-1111-1111-00000000000a', 'ACCEPTED', NOW() - INTERVAL '48 days'),
    ('22222222-2222-2222-2222-00000000000c', '11111111-1111-1111-1111-000000000008', '11111111-1111-1111-1111-000000000009', 'ACCEPTED', NOW() - INTERVAL '45 days'),
    ('22222222-2222-2222-2222-00000000000d', '11111111-1111-1111-1111-000000000009', '11111111-1111-1111-1111-00000000000b', 'ACCEPTED', NOW() - INTERVAL '40 days'),
    ('22222222-2222-2222-2222-00000000000e', '11111111-1111-1111-1111-00000000000a', '11111111-1111-1111-1111-00000000000b', 'ACCEPTED', NOW() - INTERVAL '35 days'),
    ('22222222-2222-2222-2222-00000000000f', '11111111-1111-1111-1111-00000000000b', '11111111-1111-1111-1111-00000000000c', 'ACCEPTED', NOW() - INTERVAL '30 days'),
    ('22222222-2222-2222-2222-000000000010', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-00000000000c', 'PENDING',  NOW() - INTERVAL '2 days')
ON CONFLICT (friendship_id) DO NOTHING;

-- =========================================================================
-- 6. CONVERSATIONS (private + group)
-- =========================================================================
-- We seed conversations BEFORE groups/trips because trips reference them.
INSERT INTO conversations (conversation_id, conversation_name, conversation_avatar, type, last_message, last_active_at, created_at, updated_at) VALUES
    ('33333333-3333-3333-3333-000000000001', NULL,                                       NULL,                                                              'PRIVATE', 'Ok bạn, hẹn gặp ở sân bay!',                  NOW() - INTERVAL '1 day',    NOW() - INTERVAL '30 days', NOW()),
    ('33333333-3333-3333-3333-000000000002', NULL,                                       NULL,                                                              'PRIVATE', 'Cảm ơn bạn nhiều nha ❤️',                       NOW() - INTERVAL '3 days',   NOW() - INTERVAL '40 days', NOW()),
    ('33333333-3333-3333-3333-000000000003', NULL,                                       NULL,                                                              'PRIVATE', 'Hôm nay thời tiết đẹp quá!',                   NOW() - INTERVAL '6 hours',  NOW() - INTERVAL '50 days', NOW()),
    ('33333333-3333-3333-3333-000000000004', 'Cộng đồng Trekking Tây Bắc',              'https://picsum.photos/seed/grp-taybac/200/200',                  'GROUP',   'Có ai đi Sa Pa tuần này không?',                NOW() - INTERVAL '2 hours',  NOW() - INTERVAL '60 days', NOW()),
    ('33333333-3333-3333-3333-000000000005', 'Foodie Việt Nam',                          'https://picsum.photos/seed/grp-foodie/200/200',                  'GROUP',   'Bún bò Huế ở Sài Gòn chỗ nào ngon nhỉ?',      NOW() - INTERVAL '5 hours',  NOW() - INTERVAL '45 days', NOW()),
    ('33333333-3333-3333-3333-000000000006', 'Đi biển mùa hè 2026',                      'https://picsum.photos/seed/grp-bien/200/200',                    'GROUP',   'Phú Quốc hay Nha Trang?',                       NOW() - INTERVAL '1 day',    NOW() - INTERVAL '20 days', NOW()),
    ('33333333-3333-3333-3333-000000000007', 'Photo Trip - Mây Tây Bắc',                'https://picsum.photos/seed/grp-photo/200/200',                   'GROUP',   'Sunrise ở Y Tý đỉnh lắm!',                       NOW() - INTERVAL '8 hours',  NOW() - INTERVAL '15 days', NOW()),
    ('33333333-3333-3333-3333-000000000008', NULL,                                       NULL,                                                              'PRIVATE', 'Mai lên kế hoạch cho chuyến đi nhé.',           NOW() - INTERVAL '12 hours', NOW() - INTERVAL '25 days', NOW())
ON CONFLICT (conversation_id) DO NOTHING;

-- =========================================================================
-- 7. CONVERSATION MEMBERS
-- =========================================================================
INSERT INTO conversation_members (conversation_id, user_id, role, joined_at)
SELECT cm.* FROM (
    VALUES
    ('33333333-3333-3333-3333-000000000001'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '30 days'),
    ('33333333-3333-3333-3333-000000000001'::uuid, '11111111-1111-1111-1111-000000000003'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '30 days'),
    ('33333333-3333-3333-3333-000000000002'::uuid, '11111111-1111-1111-1111-000000000004'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '40 days'),
    ('33333333-3333-3333-3333-000000000002'::uuid, '11111111-1111-1111-1111-000000000005'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '40 days'),
    ('33333333-3333-3333-3333-000000000003'::uuid, '11111111-1111-1111-1111-000000000006'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '50 days'),
    ('33333333-3333-3333-3333-000000000003'::uuid, '11111111-1111-1111-1111-000000000007'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '50 days'),
    ('33333333-3333-3333-3333-000000000004'::uuid, '11111111-1111-1111-1111-000000000009'::uuid, 'OWNER'::varchar,  NOW() - INTERVAL '60 days'),
    ('33333333-3333-3333-3333-000000000004'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '58 days'),
    ('33333333-3333-3333-3333-000000000004'::uuid, '11111111-1111-1111-1111-00000000000b'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '55 days'),
    ('33333333-3333-3333-3333-000000000004'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '50 days'),
    ('33333333-3333-3333-3333-000000000004'::uuid, '11111111-1111-1111-1111-000000000003'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '45 days'),
    ('33333333-3333-3333-3333-000000000005'::uuid, '11111111-1111-1111-1111-000000000003'::uuid, 'OWNER'::varchar,  NOW() - INTERVAL '45 days'),
    ('33333333-3333-3333-3333-000000000005'::uuid, '11111111-1111-1111-1111-000000000007'::uuid, 'ADMIN'::varchar,  NOW() - INTERVAL '44 days'),
    ('33333333-3333-3333-3333-000000000005'::uuid, '11111111-1111-1111-1111-000000000004'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '40 days'),
    ('33333333-3333-3333-3333-000000000005'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '35 days'),
    ('33333333-3333-3333-3333-000000000005'::uuid, '11111111-1111-1111-1111-000000000005'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '30 days'),
    ('33333333-3333-3333-3333-000000000006'::uuid, '11111111-1111-1111-1111-000000000008'::uuid, 'OWNER'::varchar,  NOW() - INTERVAL '20 days'),
    ('33333333-3333-3333-3333-000000000006'::uuid, '11111111-1111-1111-1111-00000000000c'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '18 days'),
    ('33333333-3333-3333-3333-000000000006'::uuid, '11111111-1111-1111-1111-000000000004'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '15 days'),
    ('33333333-3333-3333-3333-000000000006'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '10 days'),
    ('33333333-3333-3333-3333-000000000007'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'OWNER'::varchar,  NOW() - INTERVAL '15 days'),
    ('33333333-3333-3333-3333-000000000007'::uuid, '11111111-1111-1111-1111-000000000009'::uuid, 'ADMIN'::varchar,  NOW() - INTERVAL '14 days'),
    ('33333333-3333-3333-3333-000000000007'::uuid, '11111111-1111-1111-1111-000000000006'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '12 days'),
    ('33333333-3333-3333-3333-000000000007'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '10 days'),
    ('33333333-3333-3333-3333-000000000008'::uuid, '11111111-1111-1111-1111-000000000008'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '25 days'),
    ('33333333-3333-3333-3333-000000000008'::uuid, '11111111-1111-1111-1111-000000000009'::uuid, 'MEMBER'::varchar, NOW() - INTERVAL '25 days')
) AS cm(conversation_id, user_id, role, joined_at)
WHERE NOT EXISTS (
    SELECT 1 FROM conversation_members existing
    WHERE existing.conversation_id = cm.conversation_id
      AND existing.user_id = cm.user_id
);

-- =========================================================================
-- 8. CONVERSATION MESSAGES
-- =========================================================================
-- conversation_messages has no natural unique key (PK auto-generated).
-- We supply explicit IDs and use ON CONFLICT on the PK for idempotency.
INSERT INTO conversation_messages (conversation_message_id, conversation_id, sender_id, content, media_url, type, status, created_at, updated_at) VALUES
    ('33333333-aaaa-aaaa-aaaa-000000000001', '33333333-3333-3333-3333-000000000001', '11111111-1111-1111-1111-000000000002', 'Bình ơi, đi Sa Pa tuần này không?',         NULL, 'TEXT',    'READ', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
    ('33333333-aaaa-aaaa-aaaa-000000000002', '33333333-3333-3333-3333-000000000001', '11111111-1111-1111-1111-000000000003', 'Đi chứ! Mấy giờ bay?',                     NULL, 'TEXT',    'READ', NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'),
    ('33333333-aaaa-aaaa-aaaa-000000000003', '33333333-3333-3333-3333-000000000001', '11111111-1111-1111-1111-000000000002', 'Book vé 6h sáng nhé, rẻ hơn.',             NULL, 'TEXT',    'READ', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
    ('33333333-aaaa-aaaa-aaaa-000000000004', '33333333-3333-3333-3333-000000000001', '11111111-1111-1111-1111-000000000003', 'Ok bạn, hẹn gặp ở sân bay!',               NULL, 'TEXT',    'READ', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
    ('33333333-aaaa-aaaa-aaaa-000000000005', '33333333-3333-3333-3333-000000000004', '11111111-1111-1111-1111-000000000009', 'Có ai đi Sa Pa tuần này không?',           NULL, 'TEXT',    'READ', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    ('33333333-aaaa-aaaa-aaaa-000000000006', '33333333-3333-3333-3333-000000000004', '11111111-1111-1111-1111-00000000000a', 'Em đi! Mê săn mây Y Tý quá.',              NULL, 'TEXT',    'READ', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    ('33333333-aaaa-aaaa-aaaa-000000000007', '33333333-3333-3333-3333-000000000005', '11111111-1111-1111-1111-000000000003', 'Bún bò Huế ở Sài Gòn chỗ nào ngon nhỉ?',  NULL, 'TEXT',    'READ', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours'),
    ('33333333-aaaa-aaaa-aaaa-000000000008', '33333333-3333-3333-3333-000000000005', '11111111-1111-1111-1111-000000000007', 'Quán Bún Bò ở Bình Thạnh, review 5*!',     NULL, 'TEXT',    'READ', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),
    ('33333333-aaaa-aaaa-aaaa-000000000009', '33333333-3333-3333-3333-000000000006', '11111111-1111-1111-1111-000000000008', 'Phú Quốc hay Nha Trang?',                  NULL, 'TEXT',    'READ', NOW() - INTERVAL '1 day',   NOW() - INTERVAL '1 day'),
    ('33333333-aaaa-aaaa-aaaa-00000000000a', '33333333-3333-3333-3333-000000000006', '11111111-1111-1111-1111-00000000000c', 'Phú Quốc nhé! Biển đẹp, resort mới.',       NULL, 'TEXT',    'READ', NOW() - INTERVAL '23 hours',NOW() - INTERVAL '23 hours'),
    ('33333333-aaaa-aaaa-aaaa-00000000000b', '33333333-3333-3333-3333-000000000007', '11111111-1111-1111-1111-00000000000a', 'Sunrise ở Y Tý đỉnh lắm!',                  'https://images.unsplash.com/photo-1487621167305-5d248087c724?w=600&h=400&fit=crop','IMAGE','READ', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '8 hours'),
    ('33333333-aaaa-aaaa-aaaa-00000000000c', '33333333-3333-3333-3333-000000000008', '11111111-1111-1111-1111-000000000008', 'Mai lên kế hoạch cho chuyến đi nhé.',       NULL, 'TEXT',    'READ', NOW() - INTERVAL '12 hours',NOW() - INTERVAL '12 hours')
ON CONFLICT (conversation_message_id) DO NOTHING;

-- =========================================================================
-- 9. GROUPS
-- =========================================================================
INSERT INTO groups (group_id, group_name, group_description, cover_image_url, member_count, privacy, location, tags, created_at, updated_at, last_activity_at) VALUES
    ('44444444-4444-4444-4444-000000000001', 'Trekking Tây Bắc',         'Cộng đồng yêu trekking vùng Tây Bắc. Chia sẻ lịch trình, ảnh đẹp và tips săn mây.',          'https://images.unsplash.com/photo-1551632811-561732d1e306?w=1200&h=400&fit=crop', 5,  'PUBLIC',  'Sa Pa',         'trekking,tay-bac,san-may',         NOW() - INTERVAL '60 days', NOW(), NOW() - INTERVAL '2 hours'),
    ('44444444-4444-4444-4444-000000000002', 'Foodie Việt Nam',           'Chia sẻ quán ăn ngon khắp 3 miền.',                                                                'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=1200&h=400&fit=crop', 5,  'PUBLIC',  'Toàn quốc',     'am-thuc,review,food-blog',          NOW() - INTERVAL '45 days', NOW(), NOW() - INTERVAL '4 hours'),
    ('44444444-4444-4444-4444-000000000003', 'Biển Đảo Việt Nam',        'Khám phá các bãi biển đẹp, resort mới, hoạt động lặn biển.',                                     'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=1200&h=400&fit=crop', 4,  'PUBLIC',  'Toàn quốc',     'bien,dao,resort',                   NOW() - INTERVAL '20 days', NOW(), NOW() - INTERVAL '1 day'),
    ('44444444-4444-4444-4444-000000000004', 'Photo Trip - Mây Và Núi',  'Cộng đồng nhiếp ảnh gia du lịch, săn mây Tây Bắc, bình minh biển Đông.',                          'https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?w=1200&h=400&fit=crop', 4, 'PUBLIC',  'Tây Bắc',       'nhiep-anh,san-may,binh-minh',      NOW() - INTERVAL '15 days', NOW(), NOW() - INTERVAL '8 hours'),
    ('44444444-4444-4444-4444-000000000005', 'Phượt Miền Tây',           'Hành trình phượt miền Tây sông nước, chợ nổi, vườn trái cây.',                                      'https://images.unsplash.com/photo-1528127269322-539801943592?w=1200&h=400&fit=crop', 3, 'PUBLIC',  'Miền Tây',      'phuot,mien-tay,cho-noi',           NOW() - INTERVAL '10 days', NOW(), NOW() - INTERVAL '2 days')
ON CONFLICT (group_id) DO NOTHING;

-- =========================================================================
-- 10. GROUP MEMBERS
-- =========================================================================
INSERT INTO group_members (group_id, user_id, status, role, joined_at)
SELECT gm.* FROM (
    VALUES
    ('44444444-4444-4444-4444-000000000001'::uuid, '11111111-1111-1111-1111-000000000009'::uuid, 'APPROVED'::varchar, 'OWNER'::varchar,     NOW() - INTERVAL '60 days'),
    ('44444444-4444-4444-4444-000000000001'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'APPROVED'::varchar, 'MODERATOR'::varchar, NOW() - INTERVAL '58 days'),
    ('44444444-4444-4444-4444-000000000001'::uuid, '11111111-1111-1111-1111-00000000000b'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '55 days'),
    ('44444444-4444-4444-4444-000000000001'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '50 days'),
    ('44444444-4444-4444-4444-000000000001'::uuid, '11111111-1111-1111-1111-000000000003'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '45 days'),
    ('44444444-4444-4444-4444-000000000002'::uuid, '11111111-1111-1111-1111-000000000003'::uuid, 'APPROVED'::varchar, 'OWNER'::varchar,     NOW() - INTERVAL '45 days'),
    ('44444444-4444-4444-4444-000000000002'::uuid, '11111111-1111-1111-1111-000000000007'::uuid, 'APPROVED'::varchar, 'ADMIN'::varchar,     NOW() - INTERVAL '44 days'),
    ('44444444-4444-4444-4444-000000000002'::uuid, '11111111-1111-1111-1111-000000000004'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '40 days'),
    ('44444444-4444-4444-4444-000000000002'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '35 days'),
    ('44444444-4444-4444-4444-000000000002'::uuid, '11111111-1111-1111-1111-000000000005'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '30 days'),
    ('44444444-4444-4444-4444-000000000003'::uuid, '11111111-1111-1111-1111-000000000008'::uuid, 'APPROVED'::varchar, 'OWNER'::varchar,     NOW() - INTERVAL '20 days'),
    ('44444444-4444-4444-4444-000000000003'::uuid, '11111111-1111-1111-1111-00000000000c'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '18 days'),
    ('44444444-4444-4444-4444-000000000003'::uuid, '11111111-1111-1111-1111-000000000004'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '15 days'),
    ('44444444-4444-4444-4444-000000000003'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '10 days'),
    ('44444444-4444-4444-4444-000000000004'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'APPROVED'::varchar, 'OWNER'::varchar,     NOW() - INTERVAL '15 days'),
    ('44444444-4444-4444-4444-000000000004'::uuid, '11111111-1111-1111-1111-000000000009'::uuid, 'APPROVED'::varchar, 'ADMIN'::varchar,     NOW() - INTERVAL '14 days'),
    ('44444444-4444-4444-4444-000000000004'::uuid, '11111111-1111-1111-1111-000000000006'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '12 days'),
    ('44444444-4444-4444-4444-000000000004'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '10 days'),
    ('44444444-4444-4444-4444-000000000005'::uuid, '11111111-1111-1111-1111-000000000007'::uuid, 'APPROVED'::varchar, 'OWNER'::varchar,     NOW() - INTERVAL '10 days'),
    ('44444444-4444-4444-4444-000000000005'::uuid, '11111111-1111-1111-1111-00000000000a'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '8 days'),
    ('44444444-4444-4444-4444-000000000005'::uuid, '11111111-1111-1111-1111-000000000002'::uuid, 'APPROVED'::varchar, 'MEMBER'::varchar,    NOW() - INTERVAL '5 days')
) AS gm(group_id, user_id, status, role, joined_at)
WHERE NOT EXISTS (
    SELECT 1 FROM group_members existing
    WHERE existing.group_id = gm.group_id
      AND existing.user_id = gm.user_id
);

-- =========================================================================
-- 11. POSTS
-- =========================================================================
-- 15 posts spread across users and 2 group posts. like_count/comment_count
-- are denormalized; the actual content_likes/content_comments tables are
-- seeded separately to keep counts consistent.
INSERT INTO posts (post_id, user_id, content, location, group_id, like_count, comment_count, share_count, is_share, post_type, privacy, created_at, updated_at) VALUES
    ('55555555-5555-5555-5555-000000000001', '11111111-1111-1111-1111-000000000002', E'Vừa lên Y Tý săn mây, 5h sáng nhiệt độ 6-8 độ mà đáng giá từng giây. Mây cuộn như biển trắng xoá dưới chân, đỉnh đèo là nơi ngắm rõ nhất.\n\nTip nhỏ:\n- Xuất phát lúc 4h từ thị trấn Bát Xát\n- Mang theo áo khoác dày, găng tay, mũ len\n- Ống kính tele chụp mây rất đẹp\n- Sau 8h mây tan, quay về nghỉ ngơi\n\nBạn nào thích săn mây thì note lại tháng 9-11 là đẹp nhất nhé!', 'Y Tý, Lào Cai', NULL, 3, 2, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '5 days', NOW()),
    ('55555555-5555-5555-5555-000000000002', '11111111-1111-1111-1111-000000000002', E'Mùa nước đổ tháng 5 ở Mù Cang Chải đẹp mê hồn. Đứng giữa ruộng bậc thang, nghe tiếng nước chảy róc rách, mùi đất ẩm - cảm giác bình yên đến lạ.\n\nHành trình gợi ý:\n- Đèo Khau Phạ - điểm ngắm đẹp nhất\n- Bản Lìm Mông, Tú Lệ - văn hoá Thái\n- Đêm ở homestay bản địa, thịt nướng + rượu ngô\n\nBudget ~1.5tr/người cho 2N1Đ full bao gồm xe, ăn, ở. Đáng đồng tiền!', 'Mù Cang Chải, Yên Bái', NULL, 5, 1, 1, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '4 days', NOW()),
    ('55555555-5555-5555-5555-000000000003', '11111111-1111-1111-1111-000000000003', E'Sài Gòn về đêm đẹp không góc chết. Tối nay đi dạo phố đi bộ Nguyễn Huệ, ngồi rooftop Chill Bar ngắm Landmark 81 sáng đèn.\n\nĂn gì ở Sài Gòn đêm:\n- Bún bò ở đường Bạch Đằng (mở đến 2h sáng)\n- Ốc đêm vỉa hè Phạm Văn Đồng\n- Cà phê bệt trước Nhà thờ Đức Bà\n\nThành phố này không ngủ, mà càng đêm càng lung linh.', 'TP. Hồ Chí Minh', NULL, 2, 0, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '4 days', NOW()),
    ('55555555-5555-5555-5555-000000000004', '11111111-1111-1111-1111-000000000004', E'Cầu Vàng Bà Nà Hills lúc 6h sáng không một bóng người - đẹp như cõi thần tiên. Đi cáp treo lên đỉnh Bà Nà sáng sớm, mây mù bao phủ, đôi khi thấy cầu nổi giữa trời mây.\n\nLưu ý:\n- Mua vé online trước giá rẻ hơn 30%\n- Mặc áo khoác vì trên đỉnh lạnh 16-20 độ\n- Đi sáng sớm hoặc chiều muộn tránh đoàn đông\n- Ăn buffet Bà Nà cũng OK nhưng không ngon lắm, khuyến khích ăn ở Đà Nẵng rồi lên', 'Bà Nà Hills, Đà Nẵng', NULL, 8, 3, 2, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '3 days', NOW()),
    ('55555555-5555-5555-5555-000000000005', '11111111-1111-1111-1111-000000000005', E'Huế về đêm thơ mộng quá. Sông Hương lững lờ, cầu Trường Tiền rực đèn, bên kia là Đại Nội cổ kính.\n\nMình gợi ý lịch trình 1 ngày ở Huế:\n- Sáng: Đại Nội + Bảo tàng Cổ vật Cung đình\n- Trưa: Bún bò Huế gốc ở đường Lý Thường Kiệt\n- Chiều: Lăng Tự Đức, Chùa Thiên Mụ\n- Tối: Ca Huế trên sông Hương + ăn cơm hến\n\nNhớ ghé Đại Nội vào buổi tối có đèn lồng đỏ rất đẹp!', 'Huế', NULL, 4, 1, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '3 days', NOW()),
    ('55555555-5555-5555-5555-000000000006', '11111111-1111-1111-1111-000000000006', E'Hải Phòng 6h sáng sương mù dày đặc. Đi dọc đê biển Đồ Sơn, mịt mờ như Đông Dương cách đây trăm năm.\n\nĐây là lý do Hải Phòng lọt top thành phố chụp ảnh đẹp nhất VN. Ai mê chụp phong cảnh buồn, vibe vintage thì note lại:\n- Đồ Sơn 5-7h sáng\n- Cát Bà 4-6h sáng săn mây\n- Vịnh Lan Hạ lúc bình minh\n\nChụp xong ghé bánh đa cua Bàng Cát - đặc sản đáng thử 1 lần.', 'Hải Phòng', NULL, 1, 0, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '2 days', NOW()),
    ('55555555-5555-5555-5555-000000000007', '11111111-1111-1111-1111-000000000007', E'5h sáng ra chợ nổi Cái Răng, cảm giác như lạc vào thế giới khác. Ghe chở đầy trái cây, khoai môn, dừa tươi - mọi thứ đều tươi roi rói.\n\nĐi chợ nổi Cần Thơ cần biết:\n- Ra từ 5h, về lúc 7h là tan chợ\n- Ăn bún riêu, hủ tiếu ngay trên ghe\n- Mua trái cây theo mùa giá rẻ bằng 1/3 siêu thị\n- Đi cùng người biết chợ hoặc book tour mini (~150k)\n\nAi chưa đi miền Tây thì note lại nhé, văn hoá sông nước rất đặc sắc!', 'Cần Thơ', NULL, 6, 4, 1, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '2 days', NOW()),
    ('55555555-5555-5555-5555-000000000008', '11111111-1111-1111-1111-000000000008', E'Hôm nay lặn ở Hòn Mun - visibility 20m, nước trong vắt như pha lê. San hô đủ màu, cá bướm, cá mú, thỉnh thoảng gặp rùa biển.\n\nLặn Nha Trang cho người mới:\n- Cert Open Water 3-4 ngày (~4-6 triệu)\n- Fun dive 1 lần ~800k-1.2tr\n- Đi Hòn Mun, Hòn Tằm - gần, đẹp\n- Tháng 4-9 là mùa lặn đẹp nhất, biển lặng\n\nĐã mê thì không dứt ra được, đỉnh thật sự!', 'Nha Trang', NULL, 3, 1, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '2 days', NOW()),
    ('55555555-5555-5555-5555-000000000009', '11111111-1111-1111-1111-000000000009', E'Sa Pa mùa lúa chín cuối tháng 9 - bắt đầu tháng 10. Toàn bộ thung lũng Mường Hoa vàng óng, sáng sớm mây mù phủ kín.\n\nLịch trình Sa Pa 3N2Đ mình hay làm:\n- Ngày 1: Hà Nội → Sa Pa, đi bản Cát Cát\n- Ngày 2: Trekking Tả Van - Lao Chải - Tả Phìn\n- Ngày 3: Sáng Fansipan, chiều về HN\n\nLưu ý: đầu tháng 10 mưa nhiều, cứ canh giữa tháng 9 hoặc giữa tháng 10 cho chắc. Áo khoác là bắt buộc nhé!', 'Sa Pa, Lào Cai', NULL, 7, 5, 3, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '1 day', NOW()),
    ('55555555-5555-5555-5555-00000000000a', '11111111-1111-1111-1111-00000000000a', E'5h sáng Y Tý, nhiệt độ 8 độ, mây cuộn như biển trắng xoá. Đứng trên đèo ngắm mây, cảm giác như lạc giữa cõi thần tiên.\n\nSetup chụp ảnh của mình hôm nay:\n- Sony A7 IV + 24-70 f/2.8\n- Tripod nhỏ, filter CPL\n- Pin phụ: trời lạnh pin tụt nhanh\n- Cứ 10 phút chụp 1 shot vì mây thay đổi liên tục\n\nMấy ai đam mê nhiếp ảnh phong cảnh thì Y Tý mùa săn mây là must-try!', 'Y Tý, Lào Cai', NULL, 5, 2, 1, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '1 day', NOW()),
    ('55555555-5555-5555-5555-00000000000b', '11111111-1111-1111-1111-00000000000b', E'Đà Lạt mùa mưa - mình lại thích nhất. Khách vắng, hoa đẹp hơn, giá phòng rẻ, café view thung lũng cực chill.\n\nĐang ở homestay view thung lũng 250k/đêm. Sáng ngồi ban công uống cà phê ngắm mây, chiều đi chợ đêm, tối đốt lửa trại cùng mấy bạn cùng phòng.\n\nTip mùa mưa Đà Lạt:\n- Tháng 6-9 mưa nhiều, nhưng không kéo dài\n- Mang áo khoác ấm, đêm xuống 14-16 độ\n- Hạn chế đi ngoài trời 3-5h chiều (mưa to)\n- Note: cafe view đẹp - The Married Beans, TUI BLUE, La Đà Lạt', 'Đà Lạt', NULL, 4, 1, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '1 day', NOW()),
    ('55555555-5555-5555-5555-00000000000c', '11111111-1111-1111-1111-00000000000c', E'Tháng thứ 2 làm việc tại Phú Quốc. Coworking cách biển 100m, ngồi code nghe sóng vỗ, trưa bơi biển 30 phút, chiều tiếp tục.\n\nSetup Phú Quốc cho digital nomad:\n- Coworking: Wowo, The Hub Phú Quốc (~50k/ngày bao gồm cafe)\n- Internet ổn 50-100Mbps\n- Ăn: hải sản Dương Đông giá rẻ, bún quậy, ghẹ hấp\n- Ở: thuê villa 4-6tr/tháng view biển\n- Visa: 30 ngày free, sau đó gia hạn hoặc visa run\n\nAi muốn work from anywhere thì Phú Quốc là lựa chọn hợp lý. Việt Nam có wifi tốt, đồ ăn ngon, con người thân thiện!', 'Phú Quốc', NULL, 2, 0, 0, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '12 hours', NOW()),
    ('55555555-5555-5555-5555-00000000000d', '11111111-1111-1111-1111-000000000009', E'[Hướng dẫn] 5 cung trekking đẹp nhất Tây Bắc cho người mới bắt đầu:\n\n1. **Tả Van - Lao Chải - Tả Phìn (Sa Pa)** - 1 ngày, 8km, dễ, có thể book homestay bản địa\n2. **Bản Lìm Mông (Mù Cang Chải)** - 2N1Đ, 12km, trung bình, qua ruộng bậc thang mùa nước đổ\n3. **Y Tý - Sáng Ma Sáo (Lào Cai)** - 2N1Đ, 14km, khó hơn, săn mây đỉnh\n4. **Pù Luông (Thanh Hoá)** - 2N1Đ, 15km, trung bình, bản Thái đẹp\n5. **Hoàng Su Phi (Hà Giang)** - 3N2Đ, 25km, khó, ruộng bậc thang đẹp nhất VN\n\nTất cả có thể tự đi nếu chuẩn bị kỹ, hoặc book tour 1.5-3tr/người tuỳ cung. Ai thích leo núi thì cứ Tây Bắc mà quẩy!', 'Tây Bắc', '44444444-4444-4444-4444-000000000001', 12, 6, 4, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '6 hours', NOW()),
    ('55555555-5555-5555-5555-00000000000e', '11111111-1111-1111-1111-000000000003', E'[Ăn gì ở Sài Gòn] Top 10 quán ăn ngon phải thử một lần - tổng hợp từ trải nghiệm 5 năm ăn uống của mình:\n\n**Bún bò Huế gốc:**\n- Bún bò 14 (Bình Thạnh)\n- Bún bò Mụ Reệt (quận 3)\n\n**Cơm tấm:**\n- Cơm tấm Ba Ghiền (quận 10)\n- Cơm tấm Phúc Lộc Thọ\n\n**Hủ tiếu Nam Vang:**\n- Hủ tiếu Hạnh (quận 5)\n\n**Phở bò viên:**\n- Phở Hòa Pasteur\n\n**Bánh mì:**\n- Bánh mì Huỳnh Hoa (quận 1)\n\n**Hải sản:**\n- Ốc đêm vỉa hè Phạm Văn Đồng\n\nBonus: hủ tiếu gõ, bò tơ Năm Sánh, cà phê vợt đường Đào Duy Từ. Ăn Sài Gòn là cả một nghệ thuật!', 'TP. Hồ Chí Minh', '44444444-4444-4444-4444-000000000002', 9, 3, 2, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '4 hours', NOW()),
    ('55555555-5555-5555-5555-00000000000f', '11111111-1111-1111-1111-000000000008', E'[Cẩm nang] Lặn biển Phú Quốc cho người mới đầy đủ từ A-Z:\n\n**Cert & thiết bị:**\n- PADI Open Water: 3-4 ngày, ~6-8 triệu\n- SSI cũng OK, tương đương PADI\n- Nên học ở Nha Trang (rẻ) rồi mới đi Phú Quốc (đắt hơn)\n\n**Fun dive (có cert rồi):**\n- 1 lần ~1.2-1.8 triệu (gồm 2 bình)\n- Hòn Thơm, Hòn Rỏi, Bãi Bớn - top đẹp\n\n**Mùa lặn:** tháng 11 đến tháng 5 năm sau. Mùa mưa tháng 6-10 biển động, ít visibility.\n\n**Lưu ý:**\n- Bệnh tim, hen suyễn, mang thai KHÔNG nên lặn\n- Ăn nhẹ trước 1-2 tiếng\n- Hydrat hoá sau lặn\n- Không bay máy bay trong 18h sau khi lặn\n\nPhú Quốc đẹp dưới nước không kém gì trên cạn. Trải nghiệm một lần nhớ cả đời!', 'Phú Quốc', '44444444-4444-4444-4444-000000000003', 5, 2, 1, false, 'NORMAL', 'PUBLIC', NOW() - INTERVAL '1 day', NOW())
ON CONFLICT (post_id) DO NOTHING;

-- =========================================================================
-- 12. POST MEDIA
-- =========================================================================
INSERT INTO content_media (media_id, post_id, url, type, uploaded_at) VALUES
    -- post 01: Săn mây Y Tý (3 ảnh mây cuộn + núi)
    ('66666666-6666-6666-6666-000000000001', '55555555-5555-5555-5555-000000000001', 'https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '5 days'),
    ('66666666-6666-6666-6666-000000000002', '55555555-5555-5555-5555-000000000001', 'https://images.unsplash.com/photo-1487621167305-5d248087c724?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '5 days'),
    ('66666666-6666-6666-6666-000000000003', '55555555-5555-5555-5555-000000000001', 'https://images.unsplash.com/photo-1480497490787-505ec076689f?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '5 days'),
    -- post 02: Mù Cang Chải ruộng bậc thang
    ('66666666-6666-6666-6666-000000000004', '55555555-5555-5555-5555-000000000002', 'https://images.unsplash.com/photo-1573270689103-d7a4e42b609a?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '4 days'),
    -- post 03: Sài Gòn về đêm
    ('66666666-6666-6666-6666-000000000005', '55555555-5555-5555-5555-000000000003', 'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '4 days'),
    -- post 04: Cầu Vàng Đà Nẵng
    ('66666666-6666-6666-6666-000000000006', '55555555-5555-5555-5555-000000000004', 'https://images.unsplash.com/photo-1528909514045-2fa4ac7a08ba?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '3 days'),
    -- post 05: Huế cố đô
    ('66666666-6666-6666-6666-000000000007', '55555555-5555-5555-5555-000000000005', 'https://images.unsplash.com/photo-1542640244-7e672d6cef4e?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '3 days'),
    -- post 06: Hải Phòng sương mù
    ('66666666-6666-6666-6666-000000000008', '55555555-5555-5555-5555-000000000006', 'https://images.unsplash.com/photo-1480497490787-505ec076689f?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '2 days'),
    -- post 07: Chợ nổi Cái Răng
    ('66666666-6666-6666-6666-000000000009', '55555555-5555-5555-5555-000000000007', 'https://images.unsplash.com/photo-1528127269322-539801943592?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '2 days'),
    ('66666666-6666-6666-6666-00000000000a', '55555555-5555-5555-5555-000000000007', 'https://images.unsplash.com/photo-1508433957232-3107f5fd5995?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '2 days'),
    -- post 08: Lặn san hô Nha Trang
    ('66666666-6666-6666-6666-00000000000b', '55555555-5555-5555-5555-000000000008', 'https://images.unsplash.com/photo-1582967788606-a171c1080cb0?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '2 days'),
    ('66666666-6666-6666-6666-00000000000c', '55555555-5555-5555-5555-000000000008', 'https://images.unsplash.com/photo-1519046904884-53103b34b206?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '2 days'),
    -- post 09: Sa Pa mùa lúa chín
    ('66666666-6666-6666-6666-00000000000d', '55555555-5555-5555-5555-000000000009', 'https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '1 day'),
    -- post 10: Y Tý lạnh săn mây
    ('66666666-6666-6666-6666-00000000000e', '55555555-5555-5555-5555-00000000000a', 'https://images.unsplash.com/photo-1500964757637-c85e8a162699?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '1 day'),
    ('66666666-6666-6666-6666-00000000000f', '55555555-5555-5555-5555-00000000000a', 'https://images.unsplash.com/photo-1542202229-7d93c33f5d07?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '1 day'),
    -- post 11: Đà Lạt cafe view thung lũng
    ('66666666-6666-6666-6666-000000000010', '55555555-5555-5555-5555-00000000000b', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '1 day'),
    -- post 12: Coworking Phú Quốc
    ('66666666-6666-6666-6666-000000000011', '55555555-5555-5555-5555-00000000000c', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '12 hours'),
    -- post 13: Trekking Tây Bắc
    ('66666666-6666-6666-6666-000000000012', '55555555-5555-5555-5555-00000000000d', 'https://images.unsplash.com/photo-1551632811-561732d1e306?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '6 hours'),
    -- post 14: Top 10 quán ăn Sài Gòn (phở / cơm tấm)
    ('66666666-6666-6666-6666-000000000013', '55555555-5555-5555-5555-00000000000e', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '4 hours'),
    -- post 15: Lặn biển Phú Quốc (underwater)
    ('66666666-6666-6666-6666-000000000014', '55555555-5555-5555-5555-00000000000f', 'https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=1200&h=800&fit=crop', 'IMAGE', NOW() - INTERVAL '1 day')
ON CONFLICT (media_id) DO NOTHING;

-- =========================================================================
-- 13. POST TAGS
-- =========================================================================
INSERT INTO post_tags (post_id, tag_id)
SELECT p.post_id, t.tag_id
FROM (VALUES
    ('55555555-5555-5555-5555-000000000001'::uuid, 'dong-tay-bac'),
    ('55555555-5555-5555-5555-000000000001'::uuid, 'nui-rung'),
    ('55555555-5555-5555-5555-000000000001'::uuid, 'nhiep-anh'),
    ('55555555-5555-5555-5555-000000000002'::uuid, 'nui-rung'),
    ('55555555-5555-5555-5555-000000000002'::uuid, 'phuot'),
    ('55555555-5555-5555-5555-000000000003'::uuid, 'check-in'),
    ('55555555-5555-5555-5555-000000000004'::uuid, 'check-in'),
    ('55555555-5555-5555-5555-000000000004'::uuid, 'mien-trung'),
    ('55555555-5555-5555-5555-000000000005'::uuid, 'van-hoa'),
    ('55555555-5555-5555-5555-000000000005'::uuid, 'lich-su'),
    ('55555555-5555-5555-5555-000000000006'::uuid, 'nhiep-anh'),
    ('55555555-5555-5555-5555-000000000007'::uuid, 'mien-tay'),
    ('55555555-5555-5555-5555-000000000007'::uuid, 'van-hoa'),
    ('55555555-5555-5555-5555-000000000008'::uuid, 'bien-dao'),
    ('55555555-5555-5555-5555-000000000008'::uuid, 'mao-hiem'),
    ('55555555-5555-5555-5555-000000000009'::uuid, 'dong-tay-bac'),
    ('55555555-5555-5555-5555-000000000009'::uuid, 'mua-thu'),
    ('55555555-5555-5555-5555-00000000000a'::uuid, 'nhiep-anh'),
    ('55555555-5555-5555-5555-00000000000a'::uuid, 'dong-tay-bac'),
    ('55555555-5555-5555-5555-00000000000b'::uuid, 'homestay'),
    ('55555555-5555-5555-5555-00000000000b'::uuid, 'gia-dinh'),
    ('55555555-5555-5555-5555-00000000000c'::uuid, 'bien-dao'),
    ('55555555-5555-5555-5555-00000000000d'::uuid, 'phuot'),
    ('55555555-5555-5555-5555-00000000000d'::uuid, 'dong-tay-bac'),
    ('55555555-5555-5555-5555-00000000000e'::uuid, 'am-thuc'),
    ('55555555-5555-5555-5555-00000000000f'::uuid, 'bien-dao'),
    ('55555555-5555-5555-5555-00000000000f'::uuid, 'mao-hiem')
) AS p(post_id, slug)
JOIN tags t ON t.slug = p.slug
ON CONFLICT DO NOTHING;

-- =========================================================================
-- 14. CONTENT LIKES
-- =========================================================================
-- Generated procedurally but with a deterministic ordering (hash-based)
-- so the same users like the same posts every run. The number of likes per
-- post is capped at the post's denormalized like_count to keep counts in
-- sync. content_likes has no UNIQUE on (post_id, user_id), so we guard
-- with WHERE NOT EXISTS.
INSERT INTO content_likes (post_id, user_id, created_at)
SELECT p.post_id, u.user_id, NOW() - INTERVAL '5 days'
FROM posts p
CROSS JOIN LATERAL (
    SELECT user_id FROM users
    WHERE user_id <> p.user_id
    ORDER BY md5(p.post_id::text || user_id::text)
    LIMIT LEAST(p.like_count, 11)
) u
WHERE NOT EXISTS (
    SELECT 1 FROM content_likes cl
    WHERE cl.post_id = p.post_id AND cl.user_id = u.user_id
);

-- =========================================================================
-- 15. CONTENT COMMENTS
-- =========================================================================
INSERT INTO content_comments (comment_id, post_id, watch_id, user_id, content, parent_comment_id, reply_count, like_count, created_at, updated_at) VALUES
    ('77777777-7777-7777-7777-000000000001', '55555555-5555-5555-5555-000000000001', NULL, '11111111-1111-1111-1111-000000000003', 'Đẹp quá! Bao giờ mình đi cùng nhé!',               NULL, 0, 0, NOW() - INTERVAL '4 days', NOW()),
    ('77777777-7777-7777-7777-000000000002', '55555555-5555-5555-5555-000000000001', NULL, '11111111-1111-1111-1111-000000000005', 'Mây cuộn như biển thật sự. Tips săn mây là gì?',  NULL, 1, 1, NOW() - INTERVAL '4 days', NOW()),
    ('77777777-7777-7777-7777-000000000003', '55555555-5555-5555-5555-000000000001', NULL, '11111111-1111-1111-1111-000000000009', 'Đi tháng 10-11, canh lúc trời mưa 1 đêm trước.',   '77777777-7777-7777-7777-000000000002', 0, 0, NOW() - INTERVAL '3 days', NOW()),
    ('77777777-7777-7777-7777-000000000004', '55555555-5555-5555-5555-000000000002', NULL, '11111111-1111-1111-1111-000000000007', 'Ruộng bậc thang mùa nước đổ đỉnh thật!',          NULL, 0, 0, NOW() - INTERVAL '3 days', NOW()),
    ('77777777-7777-7777-7777-000000000005', '55555555-5555-5555-5555-000000000004', NULL, '11111111-1111-1111-1111-000000000002', 'Cầu Vàng nên đi sáng sớm, đúng rồi bạn!',         NULL, 1, 2, NOW() - INTERVAL '2 days', NOW()),
    ('77777777-7777-7777-7777-000000000006', '55555555-5555-5555-5555-000000000004', NULL, '11111111-1111-1111-1111-000000000006', 'Review chụp ảnh Bà Nà đỉnh, like!',                NULL, 0, 1, NOW() - INTERVAL '2 days', NOW()),
    ('77777777-7777-7777-7777-000000000007', '55555555-5555-5555-5555-000000000004', NULL, '11111111-1111-1111-1111-00000000000a', 'Có nên đi Bà Nà vào mùa mưa không bạn?',           NULL, 1, 0, NOW() - INTERVAL '2 days', NOW()),
    ('77777777-7777-7777-7777-000000000008', '55555555-5555-5555-5555-000000000004', NULL, '11111111-1111-1111-1111-000000000009', 'Mùa mưa vắng, giá rẻ. Đi tháng 9-10 là hợp lý.',   '77777777-7777-7777-7777-000000000007', 0, 0, NOW() - INTERVAL '1 day',  NOW()),
    ('77777777-7777-7777-7777-000000000009', '55555555-5555-5555-5555-000000000005', NULL, '11111111-1111-1111-1111-000000000002', 'Huế ban đêm thơ mộng ghê!',                          NULL, 0, 0, NOW() - INTERVAL '2 days', NOW()),
    ('77777777-7777-7777-7777-00000000000a', '55555555-5555-5555-5555-000000000007', NULL, '11111111-1111-1111-1111-000000000003', 'Chợ nổi 5h sáng đúng không bạn?',                  NULL, 1, 1, NOW() - INTERVAL '1 day',  NOW()),
    ('77777777-7777-7777-7777-00000000000b', '55555555-5555-5555-5555-000000000007', NULL, '11111111-1111-1111-1111-000000000005', 'Mình mua trái cây ở đây được không?',               NULL, 1, 0, NOW() - INTERVAL '1 day',  NOW()),
    ('77777777-7777-7777-7777-00000000000c', '55555555-5555-5555-5555-000000000007', NULL, '11111111-1111-1111-1111-000000000006', 'Tour chợ nổi nào rẻ nhỉ? Mình muốn đi!',            NULL, 0, 0, NOW() - INTERVAL '1 day',  NOW()),
    ('77777777-7777-7777-7777-00000000000d', '55555555-5555-5555-5555-000000000007', NULL, '11111111-1111-1111-1111-000000000008', 'Bún riêu chợ nổi ngon lắm!',                        NULL, 0, 0, NOW() - INTERVAL '12 hours',NOW()),
    ('77777777-7777-7777-7777-00000000000e', '55555555-5555-5555-5555-000000000008', NULL, '11111111-1111-1111-1111-000000000004', 'Mình cũng đang học lặn, bạn chỉ giáo nhé!',         NULL, 0, 0, NOW() - INTERVAL '1 day',  NOW()),
    ('77777777-7777-7777-7777-00000000000f', '55555555-5555-5555-5555-000000000009', NULL, '11111111-1111-1111-1111-000000000002', 'Mùa lúa chín đỉnh thật, đi cuối tháng 9!',          NULL, 1, 2, NOW() - INTERVAL '12 hours',NOW()),
    ('77777777-7777-7777-7777-000000000010', '55555555-5555-5555-5555-000000000009', NULL, '11111111-1111-1111-1111-000000000003', 'Homestay ở Sa Pa nào đẹp nhỉ?',                     NULL, 1, 1, NOW() - INTERVAL '10 hours',NOW()),
    ('77777777-7777-7777-7777-000000000011', '55555555-5555-5555-5555-000000000009', NULL, '11111111-1111-1111-1111-000000000005', 'Review thêm về đặc sản Sa Pa đi bạn!',              NULL, 0, 1, NOW() - INTERVAL '8 hours', NOW()),
    ('77777777-7777-7777-7777-000000000012', '55555555-5555-5555-5555-00000000000a', NULL, '11111111-1111-1111-1111-000000000006', '8 độ mà đi săn mây, đam mê thật!',                    NULL, 0, 0, NOW() - INTERVAL '12 hours',NOW()),
    ('77777777-7777-7777-7777-000000000013', '55555555-5555-5555-5555-00000000000b', NULL, '11111111-1111-1111-1111-000000000003', 'Homestay 250k/đêm là quá hợp lý!',                  NULL, 0, 0, NOW() - INTERVAL '10 hours',NOW()),
    ('77777777-7777-7777-7777-000000000014', '55555555-5555-5555-5555-00000000000d', NULL, '11111111-1111-1111-1111-000000000002', 'Bài viết rất chi tiết, cảm ơn bạn!',                  NULL, 0, 0, NOW() - INTERVAL '5 hours', NOW()),
    ('77777777-7777-7777-7777-000000000015', '55555555-5555-5555-5555-00000000000d', NULL, '11111111-1111-1111-1111-000000000003', 'Mình thử cung Tả Van tuần sau nhé!',                  NULL, 1, 0, NOW() - INTERVAL '4 hours', NOW()),
    ('77777777-7777-7777-7777-000000000016', '55555555-5555-5555-5555-00000000000e', NULL, '11111111-1111-1111-1111-000000000002', 'Bún bò Huế quán bạn quen, đỉnh thật!',               NULL, 0, 0, NOW() - INTERVAL '3 hours', NOW()),
    ('77777777-7777-7777-7777-000000000017', '55555555-5555-5555-5555-00000000000f', NULL, '11111111-1111-1111-1111-00000000000a', 'Cert Open Water học ở đâu tốt nhỉ?',                  NULL, 0, 0, NOW() - INTERVAL '20 hours',NOW())
ON CONFLICT (comment_id) DO NOTHING;

-- =========================================================================
-- 16. BLOGS
-- =========================================================================
INSERT INTO blogs (blog_id, user_id, title, content, thumbnail_url, description, location, view_count, average_rating, total_ratings, status, is_featured, reading_time, created_at, updated_at, published_at) VALUES
    ('88888888-8888-8888-8888-000000000001', '11111111-1111-1111-1111-000000000009', 'Hướng dẫn săn mây Y Tý từ A-Z',                                  E'<h2>Y Tý là gì?</h2><p>Y Tý là một xã vùng cao của huyện Bát Xát, tỉnh Lào Cai, nằm ở độ cao trên 2.000m so với mặt nước biển. Đây là một trong những điểm săn mây đẹp nhất Việt Nam, nổi tiếng với biển mây cuộn vào sáng sớm.</p><h2>Thời điểm đi săn mây</h2><p>Mùa săn mây Y Tý thường bắt đầu từ tháng 9 đến tháng 4 năm sau. Đỉnh điểm đẹp nhất là tháng 10-12, khi mây nhiều, nhiệt độ thấp và trời quang.</p><ul><li>Tháng 9-10: mùa lúa chín, mây nhiều</li><li>Tháng 11-12: lạnh nhất (5-8 độ), mây cuộn rõ</li><li>Tháng 1-2: mùa xuân, hoa đào nở</li><li>Tháng 3-4: thời tiết ổn định, view đẹp</li></ul><h2>Đường đi Y Tý</h2><p>Từ Hà Nội đi Y Tý khoảng 350km, mất 7-8 tiếng. Có 2 cung đường phổ biến:</p><ul><li>Hà Nội → Lào Cai (cao tốc) → Bát Xát → Y Tý</li><li>Hà Nội → Sa Pa → Bát Xát → Y Tý (cung đường đèo)</li></ul><p>Lưu ý: cung đường đèo dốc cao, chỉ nên đi ban ngày vào mùa khô. Mùa mưa có nguy cơ sạt lở.</p><h2>Tips chụp ảnh mây</h2><p>Để có bức ảnh mây đẹp:</p><ul><li>Ống kính tele 70-200mm trở lên</li><li>Chụp ở chế độ manual, f/8-f/11, ISO 100-400</li><li>Mang tripod cho chụp long exposure</li><li>Canh giờ: mây đẹp nhất lúc 5h30-7h sáng</li></ul><h2>Ăn ở</h2><p>Y Tý có homestay bản địa giá 150-300k/đêm. Đồ ăn chủ yếu là cơm, thịt lợn bản, rau rừng. Nên mang theo đồ ăn nhẹ vì quán ăn ít.</p><h2>Lưu ý quan trọng</h2><ul><li>Mang áo khoác dày, găng tay, mũ len</li><li>Pin máy ảnh tụt nhanh vì trời lạnh - mang pin phụ</li><li>Điện thoại có thể mất sóng ở một số đoạn</li><li>Không nên đi 1 mình, nên có bạn đồng hành</li></ul><p>Săn mây Y Tý là một trải nghiệm khó quên. Nếu bạn thích chụp ảnh phong cảnh và chinh phục thiên nhiên, hãy note lại Y Tý cho chuyến đi sắp tới!</p>',                 'https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?w=1200&h=600&fit=crop',  'Hướng dẫn chi tiết cách săn mây tại Y Tý: thời điểm, đường đi, lưu ý thời tiết và tips chụp ảnh.',  'Y Tý, Lào Cai',         1280, 4.6, 12,  'PUBLISHED', true,  7, NOW() - INTERVAL '30 days', NOW(), NOW() - INTERVAL '30 days'),
    ('88888888-8888-8888-8888-000000000002', '11111111-1111-1111-1111-000000000007', 'Miền Tây mùa nước nổi - kinh nghiệm từ A-Z',                    E'<h2>Miền Tây mùa nước nổi</h2><p>Mùa nước nổi ở miền Tây kéo dài từ tháng 7 đến tháng 11 hàng năm, đỉnh điểm là tháng 9-10. Đây là thời điểm đồng ruộng ngập nước, cá tôm sinh sản nhiều, là mùa săn bắt cá đồng của người dân.</p><h2>Điểm đến không thể bỏ qua</h2><ul><li><strong>Chợ nổi Cái Răng (Cần Thơ):</strong> chợ nổi lớn nhất miền Tây, hoạt động từ 5h-9h sáng</li><li><strong>Vườn trái cây Vĩnh Long:</strong> tham quan vườn, thưởng thức trái cây theo mùa</li><li><strong>Rừng tràm Trà Sư (An Giang):</strong> đi xuồng xuyên rừng tràm, ngắm chim</li><li><strong>Đồng Tháp Mười:</strong> mùa nước nổi đẹp nhất, ngắm sen, bắt cá đồng</li><li><strong>Châu Đốc - Núi Sam:</strong> lễ hội đua bò Bảy Núi (tháng 9-10 âm lịch)</li></ul><h2>Phương tiện di chuyển</h2><p>Từ TP.HCM đi miền Tây có nhiều lựa chọn:</p><ul><li>Xe khách: 2.5-3.5 tiếng tuỳ điểm đến</li><li>Xe máy: tự do nhưng đường xa</li><li>Tour: tiện, giá 800k-2tr/người/ngày</li></ul><h2>Homestay miền Tây</h2><p>Homestay miền Tây thường 200-500k/đêm, bao gồm ăn sáng. Một số homestay đẹp:</p><ul><li>Homestay Mekong Rustic (Cần Thơ)</li><li>Vườn dừa Bến Tre homestay</li><li>Làng nổi Tân Lập (Long An)</li></ul><h2>Đặc sản nên thử</h2><ul><li>Cá lóc nướng trui cuốn lá lốt</li><li>Lẩu mắm miền Tây</li><li>Bún mắm miền Tây</li><li>Bánh xèo miền Tây (bánh to, nhân giá đỗ)</li><li>Chuối nếp nướng</li><li>Nước dừa tươi hái tại vườn</li></ul><p>Miền Tây mùa nước nổi là trải nghiệm văn hoá độc đáo. Nếu bạn muốn rời xa thành phố, hãy thử 1 lần!</p>', 'https://images.unsplash.com/photo-1528127269322-539801943592?w=1200&h=600&fit=crop',  'Kinh nghiệm khám phá miền Tây mùa nước nổi: chợ nổi, vườn trái cây, homestay.',                  'Cần Thơ',               870,  4.4, 8,   'PUBLISHED', true,  6, NOW() - INTERVAL '25 days', NOW(), NOW() - INTERVAL '25 days'),
    ('88888888-8888-8888-8888-000000000003', '11111111-1111-1111-1111-000000000008', 'Cẩm nang lặn biển Nha Trang cho người mới',                      E'<h2>Lặn biển Nha Trang</h2><p>Nha Trang là một trong những điểm lặn biển đẹp nhất Việt Nam với hệ sinh thái biển phong phú. Bài viết này tổng hợp toàn bộ kinh nghiệm lặn từ A-Z cho người mới bắt đầu.</p><h2>Học gì để lặn?</h2><p>Có 2 hệ thống cert phổ biến:</p><ul><li><strong>PADI:</strong> phổ biến nhất thế giới, được công nhận toàn cầu</li><li><strong>SSI:</strong> tương đương PADI, dễ học hơn</li></ul><p>Cert cơ bản nhất là Open Water (OW), cho phép lặn xuống 18m.</p><h2>Thời gian & chi phí học</h2><ul><li>Open Water: 3-4 ngày, ~4-6 triệu</li><li>Advanced Open Water: 2-3 ngày, ~3-4 triệu</li><li>Rescue Diver: 3-4 ngày, ~5-7 triệu</li></ul><h2>Trung tâm lặn uy tín ở Nha Trang</h2><ul><li>Rainbow Divers Nha Trang</li><li>Vietnam Dive Center</li><li>Angel Dive</li><li>Oceanic Dive</li></ul><h2>Dive site đẹp</h2><ul><li><strong>Hòn Mun:</strong> gần thành phố, san hô đẹp, dễ cho người mới</li><li><strong>Hòn Tằm:</strong> visibility 15-20m, cá nhiều</li><li><strong>Hòn Rỏi:</strong> cần book tour, đẹp nhất</li><li><strong>Đảo Yến:</strong> hang động dưới biển</li></ul><h2>Lưu ý quan trọng</h2><ul><li>KHÔNG lặn nếu có bệnh tim, hen, phổi, mang thai</li><li>Ăn nhẹ trước 1-2 tiếng</li><li>Hydrat hoá sau lặn</li><li>Không bay máy bay trong 18h sau khi lặn</li><li>Mang theo: kem chống nắng, áo lặn, ống thở cá nhân</li></ul><p>Lặn biển Nha Trang là trải nghiệm đáng thử ít nhất 1 lần trong đời. Hãy bắt đầu học và khám phá đại dương!</p>', 'https://images.unsplash.com/photo-1582967788606-a171c1080cb0?w=1200&h=600&fit=crop', 'Cẩm nang lặn biển Nha Trang: cert, thiết bị, dive site đẹp, giá cả.',                           'Nha Trang',             650,  4.8, 6,   'PUBLISHED', false, 5, NOW() - INTERVAL '18 days', NOW(), NOW() - INTERVAL '18 days'),
    ('88888888-8888-8888-8888-000000000004', '11111111-1111-1111-1111-000000000003', 'Top 10 quán ăn Sài Gòn phải thử 2026',                            E'<h2>Sài Gòn ăn gì?</h2><p>Sài Gòn là thiên đường ẩm thực với hàng ngàn quán ăn từ bình dân đến cao cấp. Bài viết này tổng hợp 10 quán ăn mình đã ăn nhiều lần và đánh giá ngon nhất, phù hợp cho cả du khách và người Sài Gòn chính hiệu.</p><h2>1. Bún bò 14 (Bình Thạnh)</h2><p>Quán bún bò Huế gốc nổi tiếng ở Sài Gòn. Nước dùng đậm đà, bò tái và giò heo đều tươi. Giá 45-55k/tô.</p><h2>2. Cơm tấm Ba Ghiền</h2><p>Cơm tấm sườn bì chả truyền thống. Sườn nướng than thơm phức, sốt đặc biệt. Giá 60-80k/phần.</p><h2>3. Bún mắm Cô Ba</h2><p>Bún mắm miền Tây đúng vị, mắm cá linh thật. Nổi tiếng ở Quận 6. Giá 35-45k.</p><h2>4. Hủ tiếu Hạnh (Quận 5)</h2><p>Hủ tiếu Nam Vang gia truyền hơn 30 năm. Nước dùng trong, ngọt thanh. Giá 40-55k.</p><h2>5. Phở Hòa Pasteur</h2><p>Phở bò viên, tái, gầu. Quán đông nhưng nhanh. Giá 60-90k.</p><h2>6. Bánh mì Huỳnh Hoa</h2><p>Bánh mì thập cẩm pate, chả lụa, thịt nguội. Nổi tiếng phố Tây. Giá 50-70k.</p><h2>7. Ốc đêm vỉa hè Phạm Văn Đồng</h2><p>Khu ốc đêm nổi tiếng với hàng chục quán. Ốc hấp sả, nướng mỡ hành, xào bơ tỏi. Giá 100-200k/người.</p><h2>8. Lẩu dê Phan Xích Long</h2><p>Lẩu dê đặc sản, nước dùng thơm, thịt dê tươi. Giá 250-400k/người.</p><h2>9. Bò tơ Năm Sánh</h2><p>Bò tơ nướng, lẩu bò, bò nhúng dấm. Quán nổi tiếng ở quận 1. Giá 300-500k/người.</p><h2>10. Cà phê vợt đường Đào Duy Từ</h2><p>Cà phê phin truyền thống, không gian vintage. Giá 30-50k/ly.</p><h2>Tips khi đi ăn Sài Gòn</h2><ul><li>Đi theo nhóm 4-6 người để gọi nhiều món</li><li>Mang theo Grab để di chuyển nhanh</li><li>Ăn tối sau 19h là đúng vibe Sài Gòn nhất</li><li>Tránh quán đông khách du lịch quá, ăn quán local mới ngon</li></ul><p>Sài Gòn có thể không có cảnh đẹp như Hà Nội, nhưng ẩm thực thì không thành phố nào sánh được!</p>', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=1200&h=600&fit=crop', 'Top 10 quán ăn ngon, giá hợp lý tại Sài Gòn bạn không nên bỏ lỡ.',                              'TP. Hồ Chí Minh',      2400, 4.7, 22,  'PUBLISHED', true,  8, NOW() - INTERVAL '15 days', NOW(), NOW() - INTERVAL '15 days'),
    ('88888888-8888-8888-8888-000000000005', '11111111-1111-1111-1111-000000000005', 'Cố đô Huế - 10 địa điểm check-in đẹp nhất',                     E'<h2>Cố đô Huế</h2><p>Huế - kinh đô cuối cùng của chế độ phong kiến Việt Nam - là thành phố có bề dày lịch sử và văn hoá lâu đời. Dưới đây là 10 địa điểm check-in đẹp nhất mà bạn không nên bỏ qua khi đến Huế.</p><h2>1. Đại Nội (Kinh thành Huế)</h2><p>Quần thể kiến trúc hoàng gia từ thời Nguyễn. Đặc biệt đẹp vào buổi tối khi có đèn lồng đỏ thắp sáng. Vé vào cổng 200k/người.</p><h2>2. Lăng Tự Đức</h2><p>Lăng vua có kiến trúc hài hoà với thiên nhiên, hồ Lưu Khiêm thơ mộng. Vé 150k.</p><h2>3. Chùa Thiên Mụ</h2><p>Biểu tượng của Huế với tháp Phước Duyên 7 tầng. Miễn phí vào cổng.</p><h2>4. Lăng Khải Định</h2><p>Kiến trúc Đông - Tây kết hợp độc đáo. Điểm nhấn là Cung Thiên Định dát vàng. Vé 150k.</p><h2>5. Cầu Trường Tiền</h2><p>Cầu bắc qua sông Hương, thay đổi màu sắc về đêm. Điểm check-in free và đẹp nhất vào 18h-21h.</p><h2>6. Chợ Đông Ba</h2><p>Chợ lớn nhất Huế, bán đặc sản: mè xửng, kẹo cau, nem chua, tré. Mua quà rất OK.</p><h2>7. Núi Ngự Bình</h2><p>Núi cao ở trung tâm Huế, view toàn cảnh thành phố. Leo khoảng 1 tiếng.</p><h2>8. Biển Thuận An</h2><p>Biển gần trung tâm, đi từ Đại Nội khoảng 15km. Hoàng hôn ở đây rất đẹp.</p><h2>9. Đồi Vọng Cảnh</h2><p>View nhìn toàn cảnh sông Hương và Đại Nội. Đặc biệt đẹp lúc bình minh.</p><h2>10. Hồ Tịnh Tâm</h2><p>Hồ nước trong khuôn viên Đại Nội, view yên bình. Thường kết hợp tham quan Đại Nội.</p><h2>Đặc sản Huế</h2><ul><li>Bún bò Huế (quán Hạnh, quán Mụ Reệt)</li><li>Cơm hến</li><li>Bánh bèo, nậm, lọc</li><li>Mè xửng Thiên Hương</li><li>Nem chua, tré</li><li>Cà phê Muối</li></ul><p>Huế đẹp theo cách rất riêng - trầm mặc, cổ kính nhưng không cũ kỹ. Nếu bạn thích văn hoá lịch sử, hãy đến Huế ít nhất 2 ngày!</p>', 'https://images.unsplash.com/photo-1542640244-7e672d6cef4e?w=1200&h=600&fit=crop', '10 địa điểm check-in đẹp nhất tại cố đô Huế: Đại Nội, lăng tẩm, chùa Thiên Mụ,...',            'Huế',                   1580, 4.5, 15,  'PUBLISHED', false, 6, NOW() - INTERVAL '10 days', NOW(), NOW() - INTERVAL '10 days'),
    ('88888888-8888-8888-8888-000000000006', '11111111-1111-1111-1111-00000000000a', 'Photograph mây Tây Bắc - settings & tips',                        E'<h2>Photo mây</h2><p>Chụp mây cuộn ở Tây Bắc là một trong những thể loại nhiếp ảnh phong cảnh được yêu thích nhất. Bài viết này chia sẻ kinh nghiệm chụp từ setup máy, lens, settings, thời điểm vàng và quy trình xử lý ảnh.</p><h2>Setup máy ảnh</h2><p>Máy ảnh phù hợp:</p><ul><li>Mirrorless full-frame: Sony A7 IV, Canon R5, Nikon Z7</li><li>DSLR full-frame: Canon 5D Mark IV, Nikon D850</li><li>APS-C vẫn OK: Sony A6400, Fuji X-T4</li></ul><p>Lens khuyến nghị:</p><ul><li>24-70mm f/2.8: lens chính cho phong cảnh</li><li>70-200mm f/2.8: chụp mây xa, nén phong cảnh</li><li>16-35mm f/4: chụp rộng, panorama</li></ul><h2>Phụ kiện cần mang</h2><ul><li>Tripod carbon nhẹ (dễ mang trekking)</li><li>Filter CPL (chống phản chiếu, tăng độ tương phản)</li><li>Filter ND (long exposure mây)</li><li>Pin phụ (trời lạnh pin tụt nhanh)</li><li>Thẻ nhớ 64GB+ (RAW ảnh nặng)</li><li>Khăn lau lens (sương mù dễ bám)</li></ul><h2>Settings khuyến nghị</h2><p>Chụp mây cuộn sáng sớm:</p><ul><li>Mode: Manual (M)</li><li>Aperture: f/8 - f/11</li><li>Shutter: 1/125s - 1/500s (mây chuyển động)</li><li>ISO: 100 - 400</li><li>White balance: Daylight hoặc Cloudy</li></ul><p>Chụp long exposure mây mềm:</p><ul><li>Aperture: f/11 - f/16</li><li>Shutter: 30s - 120s + filter ND 6-10 stops</li><li>ISO: 100</li></ul><h2>Thời điểm vàng</h2><ul><li><strong>Bình minh (5h30-7h):</strong> ánh sáng vàng ấm, mây cuộn rõ, sương mù nhẹ</li><li><strong>Hoàng hôn (17h-18h30):</strong> ánh sáng cam, mây nhiều màu</li><li><strong>Sau mưa:</strong> mây cuộn nhiều nhất, đặc biệt mây cuộn sau 1 đêm mưa</li></ul><h2>Địa điểm chụp mây đẹp</h2><ul><li><strong>Y Tý (Lào Cai):</strong> mây cuộn đỉnh, view rộng</li><li><strong>Sìn Hồ (Lai Châu):</strong> ít người, view hoang sơ</li><li><strong>Tà Xùa (Sơn La):</strong> mây biển, view 360 độ</li><li><strong>Mù Cang Chải (Yên Bái):</strong> mây kết hợp ruộng bậc thang</li><li><strong>Hoàng Su Phi (Hà Giang):</strong> mây mùa lúa chín</li></ul><h2>Post-processing</h2><ul><li>Adobe Lightroom: chỉnh contrast, dehaze</li><li>Photoshop: xử lý blend nhiều ảnh panorama</li><li>Luminar AI: tăng chi tiết mây</li></ul><p>Chụp mây Tây Bắc không chỉ là kỹ thuật, mà là sự kiên nhẫn và đam mê. Hãy dành thời gian ở lại ít nhất 2-3 đêm để bắt được khoảnh khắc đẹp nhất!</p>', 'https://images.unsplash.com/photo-1500964757637-c85e8a162699?w=1200&h=600&fit=crop','Tips chụp ảnh mây cuộn Tây Bắc: máy ảnh, lens, settings, thời điểm vàng.',                     'Tây Bắc',              720,  4.9, 7,   'PUBLISHED', false, 5, NOW() - INTERVAL '8 days',  NOW(), NOW() - INTERVAL '8 days')
ON CONFLICT (blog_id) DO NOTHING;

-- =========================================================================
-- 17. BLOG REVIEWS
-- =========================================================================
INSERT INTO blog_reviews (review_id, blog_id, user_id, content, rating, is_edited, created_at, updated_at) VALUES
    ('99999999-9999-9999-9999-000000000001', '88888888-8888-8888-8888-000000000001', '11111111-1111-1111-1111-000000000002', 'Bài viết rất chi tiết. Mình đã đi theo hướng dẫn và thành công!',     5, false, NOW() - INTERVAL '20 days', NOW()),
    ('99999999-9999-9999-9999-000000000002', '88888888-8888-8888-8888-000000000001', '11111111-1111-1111-1111-000000000006', 'Ảnh đẹp mê hồn, tips săn mây rất hữu ích.',                              5, false, NOW() - INTERVAL '18 days', NOW()),
    ('99999999-9999-9999-9999-000000000003', '88888888-8888-8888-8888-000000000001', '11111111-1111-1111-1111-00000000000a', 'Bạn viết như đang ở đó. Mê quá!',                                          4, false, NOW() - INTERVAL '15 days', NOW()),
    ('99999999-9999-9999-9999-000000000004', '88888888-8888-8888-8888-000000000002', '11111111-1111-1111-1111-000000000003', 'Mùa nước nổi đỉnh thật. Chợ nổi Phong Điền cũng rất hay.',                5, false, NOW() - INTERVAL '12 days', NOW()),
    ('99999999-9999-9999-9999-000000000005', '88888888-8888-8888-8888-000000000003', '11111111-1111-1111-1111-000000000005', 'Cert Open Water ở Nha Trang rẻ hơn Phú Quốc nhiều.',                       4, false, NOW() - INTERVAL '10 days', NOW()),
    ('99999999-9999-9999-9999-000000000006', '88888888-8888-8888-8888-000000000004', '11111111-1111-1111-1111-000000000007', 'Top 10 này chuẩn luôn. Bún bò quán ở Bình Thạnh ghi đúng top 1!',         5, false, NOW() - INTERVAL '8 days',  NOW()),
    ('99999999-9999-9999-9999-000000000007', '88888888-8888-8888-8888-000000000005', '11111111-1111-1111-1111-000000000004', 'Huế đẹp, bài viết cũng đẹp. Cảm ơn bạn!',                                  5, false, NOW() - INTERVAL '5 days',  NOW()),
    ('99999999-9999-9999-9999-000000000008', '88888888-8888-8888-8888-000000000006', '11111111-1111-1111-1111-000000000009', 'Tips chụp mây rất chuyên nghiệp. Recommend!',                                5, false, NOW() - INTERVAL '4 days',  NOW())
ON CONFLICT (review_id) DO NOTHING;

-- =========================================================================
-- 18. WATCHES (video content)
-- =========================================================================
INSERT INTO watches (watch_id, user_id, title, description, video_url, thumbnail_url, duration, location, privacy, like_count, comment_count, share_count, view_count, created_at, updated_at) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000001', '11111111-1111-1111-1111-00000000000a', 'Săn mây Y Tý - 4K Cinematic',         'Cinematic 4K quay mây cuộn Y Tý lúc bình minh. Edited bằng DaVinci Resolve.',  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',  'https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?w=600&h=800&fit=crop',  60,  'Y Tý, Lào Cai',        'PUBLIC', 45, 8,  5,  1200, NOW() - INTERVAL '12 days', NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000002', '11111111-1111-1111-1111-000000000007', 'Chợ nổi Cái Răng 5h sáng',            'Drone view chợ nổi Cái Răng Cần Thơ. Đẹp như phim!',                          'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4','https://images.unsplash.com/photo-1528127269322-539801943592?w=600&h=800&fit=crop', 45, 'Cần Thơ',              'PUBLIC', 32, 4,  2,  890,  NOW() - INTERVAL '10 days', NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000003', '11111111-1111-1111-1111-000000000008', 'Lặn san hô Nha Trang - underwater',   'Diving video ở Hòn Mun, Nha Trang. Visibility 20m.',                           'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4','https://images.unsplash.com/photo-1582967788606-a171c1080cb0?w=600&h=800&fit=crop', 75, 'Nha Trang',            'PUBLIC', 18, 2,  1,  560,  NOW() - INTERVAL '7 days',  NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000004', '11111111-1111-1111-1111-000000000006', 'Hải Phòng sương mù buổi sáng',        'Time-lapse sương mù Hải Phòng lúc 5h-7h sáng, đỉnh Cát Bà.',                  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4','https://images.unsplash.com/photo-1480497490787-505ec076689f?w=600&h=800&fit=crop', 30, 'Cát Bà, Hải Phòng',    'PUBLIC', 12, 1,  0,  340,  NOW() - INTERVAL '5 days',  NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000005', '11111111-1111-1111-1111-000000000004', 'Cầu Vàng Bà Nà Hills time-lapse',     'Time-lapse Cầu Vàng từ sáng đến chiều tối. Cinematic.',                        'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4',  'https://images.unsplash.com/photo-1528909514045-2fa4ac7a08ba?w=600&h=800&fit=crop', 50, 'Bà Nà Hills, Đà Nẵng', 'PUBLIC', 56, 11, 8,  2400, NOW() - INTERVAL '3 days',  NOW()),
    ('aaaaaaaa-aaaa-aaaa-aaaa-000000000006', '11111111-1111-1111-1111-00000000000b', 'Đà Lạt mùa mưa - vibe chill',          'Vlog Đà Lạt mùa mưa, café view thung lũng, hoa đẹp.',                          'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4','https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=600&h=800&fit=crop', 40, 'Đà Lạt',               'PUBLIC', 22, 3,  1,  720,  NOW() - INTERVAL '1 day',   NOW())
ON CONFLICT (watch_id) DO NOTHING;

-- =========================================================================
-- 19. SAVED WATCHES & HISTORIES
-- =========================================================================
INSERT INTO saved_watches (user_id, watch_id, created_at) VALUES
    ('11111111-1111-1111-1111-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001', NOW() - INTERVAL '10 days'),
    ('11111111-1111-1111-1111-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', NOW() - INTERVAL '3 days'),
    ('11111111-1111-1111-1111-00000000000a', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', NOW() - INTERVAL '5 days'),
    ('11111111-1111-1111-1111-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', NOW() - INTERVAL '8 days')
ON CONFLICT (user_id, watch_id) DO NOTHING;

INSERT INTO watch_histories (user_id, watch_id, created_at, updated_at) VALUES
    ('11111111-1111-1111-1111-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001', NOW() - INTERVAL '11 days', NOW()),
    ('11111111-1111-1111-1111-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', NOW() - INTERVAL '2 days',  NOW()),
    ('11111111-1111-1111-1111-000000000004', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000005', NOW() - INTERVAL '2 days',  NOW()),
    ('11111111-1111-1111-1111-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000001', NOW() - INTERVAL '11 days', NOW()),
    ('11111111-1111-1111-1111-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', NOW() - INTERVAL '9 days',  NOW()),
    ('11111111-1111-1111-1111-000000000006', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000004', NOW() - INTERVAL '4 days',  NOW()),
    ('11111111-1111-1111-1111-000000000007', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000002', NOW() - INTERVAL '9 days',  NOW()),
    ('11111111-1111-1111-1111-000000000008', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', NOW() - INTERVAL '6 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000a', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000003', NOW() - INTERVAL '6 days',  NOW()),
    ('11111111-1111-1111-1111-00000000000a', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000006', NOW() - INTERVAL '20 hours',NOW()),
    ('11111111-1111-1111-1111-00000000000b', 'aaaaaaaa-aaaa-aaaa-aaaa-000000000006', NOW() - INTERVAL '12 hours',NOW())
ON CONFLICT (user_id, watch_id) DO NOTHING;

-- =========================================================================
-- 20. TRIPS (attached to conversations)
-- =========================================================================
INSERT INTO trips (trip_id, conversation_id, trip_name, trip_description, cover_image_url, destination, start_date, end_date, budget, status, created_by, created_at, updated_at) VALUES
    ('bbbbbbbb-bbbb-bbbb-bbbb-000000000001', '33333333-3333-3333-3333-000000000004', 'Trekking Sa Pa 3N2Đ',          'Lịch trình trekking Sa Pa - Y Tý 3 ngày 2 đêm. Phù hợp người mới.',          'https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=1200&h=600&fit=crop',  'Sa Pa, Lào Cai',          NOW() + INTERVAL '14 days', NOW() + INTERVAL '16 days', 3500000.00, 'CONFIRMED', '11111111-1111-1111-1111-000000000009', NOW() - INTERVAL '20 days', NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-000000000002', '33333333-3333-3333-3333-000000000005', 'Food tour Sài Gòn 1 ngày',     'Đi 5 quán ăn nổi tiếng Sài Gòn trong 1 ngày. Bụng phải đói!',           'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=1200&h=600&fit=crop','TP. Hồ Chí Minh',       NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days',  800000.00,  'PLANNING',  '11111111-1111-1111-1111-000000000003', NOW() - INTERVAL '5 days',  NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-000000000003', '33333333-3333-3333-3333-000000000006', 'Phú Quốc 4N3Đ - Biển & Resort','Nghỉ dưỡng Phú Quốc 4N3Đ, resort 5*, lặn biển.',                          'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=1200&h=600&fit=crop',   'Phú Quốc',               NOW() + INTERVAL '30 days', NOW() + INTERVAL '33 days', 12000000.00,'CONFIRMED', '11111111-1111-1111-1111-000000000008', NOW() - INTERVAL '10 days', NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-000000000004', '33333333-3333-3333-3333-000000000007', 'Photo Trip Mù Cang Chải 2N1Đ','Săn mây + chụp ruộng bậc thang Mù Cang Chải 2N1Đ, early October.',         'https://images.unsplash.com/photo-1573270689103-d7a4e42b609a?w=1200&h=600&fit=crop',  'Mù Cang Chải, Yên Bái',  NOW() + INTERVAL '21 days', NOW() + INTERVAL '22 days', 1500000.00, 'PLANNING',  '11111111-1111-1111-1111-00000000000a', NOW() - INTERVAL '3 days',  NOW())
ON CONFLICT (trip_id) DO NOTHING;

-- =========================================================================
-- 21. TRIP SCHEDULES
-- =========================================================================
INSERT INTO trip_schedules (trip_schedule_id, trip_id, title, description, location, schedule_date, start_time, end_time, activity_type, estimated_cost, notes, order_index, created_by, created_at, updated_at) VALUES
    ('cccccccc-cccc-cccc-cccc-000000000001', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000001', 'Di chuyển Hà Nội → Sa Pa',          'Xe giường nằm Hà Nội - Sa Pa, khởi hành 22h.',                   'BX Mỹ Đình, Hà Nội',       NOW() + INTERVAL '14 days', NOW() + INTERVAL '14 days' - INTERVAL '8 hours', NOW() + INTERVAL '14 days' - INTERVAL '5 hours', 'TRANSPORT',     350000.00,  'Mang áo ấm',  0, '11111111-1111-1111-1111-000000000009', NOW() - INTERVAL '20 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000002', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000001', 'Trekking Tả Van - Lao Chải',        'Trekking nhẹ 8km qua bản Tả Van và Lao Chải.',                   'Tả Van, Sa Pa',             NOW() + INTERVAL '15 days', NOW() + INTERVAL '15 days' + INTERVAL '8 hours',  NOW() + INTERVAL '15 days' + INTERVAL '14 hours', 'VISIT',         0.00,       'Giày thể thao', 1, '11111111-1111-1111-1111-000000000009', NOW() - INTERVAL '20 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000001', 'Ăn tối nướng BBQ',                    'Buffet BBQ đêm tại nhà hàng địa phương.',                          'Sa Pa town',                NOW() + INTERVAL '15 days', NOW() + INTERVAL '15 days' + INTERVAL '18 hours', NOW() + INTERVAL '15 days' + INTERVAL '20 hours', 'MEAL',          250000.00,  NULL,         2, '11111111-1111-1111-1111-000000000009', NOW() - INTERVAL '20 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000004', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000001', 'Săn mây Y Tý sunrise',                'Xe lên Y Tý lúc 4h sáng, săn mây tại đèo.',                         'Y Tý, Lào Cai',             NOW() + INTERVAL '16 days', NOW() + INTERVAL '16 days' + INTERVAL '5 hours',  NOW() + INTERVAL '16 days' + INTERVAL '8 hours',  'VISIT',         0.00,       NULL,         3, '11111111-1111-1111-1111-000000000009', NOW() - INTERVAL '20 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000005', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000002', 'Bún bò Huế gốc',                     'Ăn sáng bún bò Huế tại quán lâu đời.',                              'Quận Bình Thạnh',           NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days' + INTERVAL '7 hours',   NOW() + INTERVAL '7 days' + INTERVAL '8 hours',   'MEAL',          45000.00,   NULL,         0, '11111111-1111-1111-1111-000000000003', NOW() - INTERVAL '5 days',  NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000006', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000002', 'Cơm tấm sườn bì',                    'Ăn trưa cơm tấm sườn bì chả.',                                       'Quận 3',                    NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days' + INTERVAL '11 hours',  NOW() + INTERVAL '7 days' + INTERVAL '12 hours',  'MEAL',          55000.00,   NULL,         1, '11111111-1111-1111-1111-000000000003', NOW() - INTERVAL '5 days',  NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000007', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000002', 'Hủ tiếu Nam Vang',                   'Ăn chiều hủ tiếu Nam Vang.',                                          'Quận 5',                    NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days' + INTERVAL '17 hours',  NOW() + INTERVAL '7 days' + INTERVAL '18 hours',  'MEAL',          50000.00,   NULL,         2, '11111111-1111-1111-1111-000000000003', NOW() - INTERVAL '5 days',  NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000008', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000003', 'Check-in resort',                     'Check-in Vinpearl Phú Quốc, nghỉ ngơi.',                              'Bãi Dài, Phú Quốc',         NOW() + INTERVAL '30 days', NOW() + INTERVAL '30 days' + INTERVAL '14 hours', NOW() + INTERVAL '30 days' + INTERVAL '15 hours', 'ACCOMMODATION', 4500000.00, 'Phòng view biển', 0, '11111111-1111-1111-1111-000000000008', NOW() - INTERVAL '10 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-000000000009', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000003', 'Lặn san hô Hòn Thơm',                'Tour lặn 1 ngày Hòn Thơm, 2 lần lặn.',                              'Hòn Thơm, Phú Quốc',        NOW() + INTERVAL '31 days', NOW() + INTERVAL '31 days' + INTERVAL '8 hours',  NOW() + INTERVAL '31 days' + INTERVAL '16 hours', 'VISIT',         1500000.00, 'Cert Open Water required', 1, '11111111-1111-1111-1111-000000000008', NOW() - INTERVAL '10 days', NOW()),
    ('cccccccc-cccc-cccc-cccc-00000000000a', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000004', 'Di chuyển Hà Nội → Mù Cang Chải',    'Xuất phát 4h sáng để kịp bình minh.',                                'Hà Nội',                    NOW() + INTERVAL '21 days', NOW() + INTERVAL '21 days' - INTERVAL '4 hours', NOW() + INTERVAL '21 days' - INTERVAL '1 hours', 'TRANSPORT',     300000.00,  'Mang áo ấm + tripod', 0, '11111111-1111-1111-1111-00000000000a', NOW() - INTERVAL '3 days',  NOW()),
    ('cccccccc-cccc-cccc-cccc-00000000000b', 'bbbbbbbb-bbbb-bbbb-bbbb-000000000004', 'Chụp ruộng bậc thang',                'Đi các điểm view ruộng bậc thang đẹp nhất.',                          'Mù Cang Chải',              NOW() + INTERVAL '21 days', NOW() + INTERVAL '21 days' + INTERVAL '6 hours',  NOW() + INTERVAL '21 days' + INTERVAL '11 hours', 'VISIT',         0.00,       NULL,         1, '11111111-1111-1111-1111-00000000000a', NOW() - INTERVAL '3 days',  NOW())
ON CONFLICT (trip_schedule_id) DO NOTHING;

-- =========================================================================
-- 22. NOTIFICATIONS
-- =========================================================================
INSERT INTO notifications (notification_id, receiver_id, sender_id, type, content, related_id, is_read, created_at, updated_at) VALUES
    ('dddddddd-dddd-dddd-dddd-000000000001', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000009', 'POST_LIKE',       'Hiếu đã thích bài viết của bạn',          '55555555-5555-5555-5555-000000000001', true,  NOW() - INTERVAL '4 days',  NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000002', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000005', 'POST_COMMENT',    'Đào đã bình luận về bài viết của bạn',    '55555555-5555-5555-5555-000000000001', true,  NOW() - INTERVAL '4 days',  NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000003', '11111111-1111-1111-1111-000000000004', '11111111-1111-1111-1111-000000000002', 'POST_LIKE',       'An đã thích bài viết của bạn',            '55555555-5555-5555-5555-000000000004', false, NOW() - INTERVAL '2 days',  NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000004', '11111111-1111-1111-1111-000000000007', '11111111-1111-1111-1111-000000000003', 'POST_COMMENT',    'Bình đã bình luận về bài viết của bạn',   '55555555-5555-5555-5555-000000000007', false, NOW() - INTERVAL '1 day',   NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000005', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-00000000000c', 'FRIEND_REQUEST',  'Khanh đã gửi lời mời kết bạn',            '22222222-2222-2222-2222-000000000010', false, NOW() - INTERVAL '2 days',  NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000006', '11111111-1111-1111-1111-000000000009', '11111111-1111-1111-1111-000000000002', 'GROUP_INVITE',    'An đã mời bạn vào nhóm',                  '44444444-4444-4444-4444-000000000001', true,  NOW() - INTERVAL '50 days', NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000007', '11111111-1111-1111-1111-000000000009', '11111111-1111-1111-1111-000000000003', 'NEW_POST',        'Bình vừa đăng bài viết mới',              '55555555-5555-5555-5555-000000000003', false, NOW() - INTERVAL '4 days',  NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000008', '11111111-1111-1111-1111-00000000000a', '11111111-1111-1111-1111-000000000009', 'POST_LIKE',       'Hiếu đã thích bài viết của bạn',          '55555555-5555-5555-5555-00000000000a', true,  NOW() - INTERVAL '12 hours',NOW()),
    ('dddddddd-dddd-dddd-dddd-000000000009', '11111111-1111-1111-1111-000000000002', '11111111-1111-1111-1111-000000000003', 'CHAT_MESSAGE',    'Bình đã gửi tin nhắn mới',                '33333333-3333-3333-3333-000000000001', false, NOW() - INTERVAL '1 day',   NOW()),
    ('dddddddd-dddd-dddd-dddd-00000000000a', '11111111-1111-1111-1111-000000000009', '11111111-1111-1111-1111-000000000008', 'GROUP_INVITE',    'Giang đã mời bạn vào nhóm',                '44444444-4444-4444-4444-000000000003', false, NOW() - INTERVAL '15 days', NOW())
ON CONFLICT (notification_id) DO NOTHING;

-- =========================================================================
-- 23. UPDATE LAST_ACTIVE_AT / counts to reflect seeded data
-- =========================================================================
-- Bump conversation last_active_at to the latest message timestamp
UPDATE conversations c
SET last_active_at = (
    SELECT MAX(created_at) FROM conversation_messages WHERE conversation_id = c.conversation_id
)
WHERE EXISTS (SELECT 1 FROM conversation_messages WHERE conversation_id = c.conversation_id);

-- Sync group member_count to actual members (defensive)
UPDATE groups g
SET member_count = (
    SELECT COUNT(*) FROM group_members WHERE group_id = g.group_id
);

-- Sync post like_count to actual content_likes count
UPDATE posts p
SET like_count = (
    SELECT COUNT(*) FROM content_likes WHERE post_id = p.post_id
);

-- Sync post comment_count to actual content_comments count
UPDATE posts p
SET comment_count = (
    SELECT COUNT(*) FROM content_comments WHERE post_id = p.post_id
);

-- Sync blog total_ratings + average_rating from blog_reviews
UPDATE blogs b
SET total_ratings = (SELECT COUNT(*) FROM blog_reviews WHERE blog_id = b.blog_id),
    average_rating = COALESCE((SELECT AVG(rating)::numeric(3,2) FROM blog_reviews WHERE blog_id = b.blog_id), 0.0);

-- Sync watch like/comment counts (denormalized counters)
UPDATE watches w
SET like_count = (SELECT COUNT(*) FROM content_likes WHERE watch_id = w.watch_id),
    comment_count = (SELECT COUNT(*) FROM content_comments WHERE watch_id = w.watch_id),
    view_count = view_count + (SELECT COUNT(*) FROM watch_histories WHERE watch_id = w.watch_id);