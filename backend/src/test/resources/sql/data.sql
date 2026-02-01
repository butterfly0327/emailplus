-- 테스트용 초기 데이터

-- 계정 데이터
INSERT INTO accounts (account_id, email, username) VALUES
(1, 'test1@example.com', 'Test User 1'),
(2, 'test2@example.com', 'Test User 2');

-- 제스처 데이터
INSERT INTO gestures (gesture_id, gesture_code,gesture_name , gesture_description) VALUES
(1, 1,'Swipe Left', 'Swipe left gesture'),
(2, 2,'Swipe Right', 'Swipe right gesture'),
(3, 3,'Swipe Up', 'Swipe up gesture'),
(4, 4,'Swipe Down', 'Swipe down gesture'),
(5, 5,'Double Tap', 'Double tap gesture');

-- 기능 데이터
INSERT INTO actions (action_id, action_name, action_category, action_description) VALUES
(1, 'Next Slide', 'PPT', 'Move to next slide'),
(2, 'Previous Slide', 'PPT', 'Move to previous slide'),
(3, 'Start Presentation', 'PPT', 'Start presentation'),
(4, 'End Presentation', 'PPT', 'End presentation'),
(5, 'Toggle Laser', 'PPT', 'Toggle laser pointer');

-- 매핑 데이터
INSERT INTO mappings (mapping_id, account_id, title, is_representative) VALUES
(1, 1, 'Default Mapping', true),
(2, 1, 'Custom Mapping', false),
(3, 2, 'User2 Mapping', true);

-- 매핑 항목 데이터
INSERT INTO mapping_items (mapping_item_id, mapping_id, gesture_id, action_id, sort_order) VALUES
(1, 1, 1, 2, 1),
(2, 1, 2, 1, 2),
(3, 2, 3, 3, 1),
(4, 3, 1, 1, 1);
