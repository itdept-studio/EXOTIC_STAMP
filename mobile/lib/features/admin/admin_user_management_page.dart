import 'package:flutter/material.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';

class AdminUserManagementPage extends StatefulWidget {
  const AdminUserManagementPage({super.key});

  @override
  State<AdminUserManagementPage> createState() =>
      _AdminUserManagementPageState();
}

class _AdminUserManagementPageState extends State<AdminUserManagementPage> {
  int _bottomNavIndex = 0;

  final List<_AdminUserItem> _users = [
    const _AdminUserItem(
      name: 'Nguyễn Văn A',
      email: 'vana@company.com',
      role: 'Admin',
      createdAt: '2023-10-15',
      isActive: true,
    ),
    const _AdminUserItem(
      name: 'Trần Thị B',
      email: 'thib@company.com',
      role: 'Editor',
      createdAt: '2023-11-20',
      isActive: true,
    ),
    const _AdminUserItem(
      name: 'Lê Hoàng C',
      email: 'hoangc@company.com',
      role: 'Viewer',
      createdAt: '2024-01-05',
      isActive: false,
    ),
    const _AdminUserItem(
      name: 'Phạm Minh D',
      email: 'minhd@company.com',
      role: 'Editor',
      createdAt: '2024-02-12',
      isActive: true,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F7F9),
      appBar: AppBar(
        toolbarHeight: 64,
        titleSpacing: 16,
        title: const Row(
          children: [
            SizedBox(
              width: 64,
              height: 64,
              child: Image(
                image: AssetImage('assets/logo/ExoticStamp_logo.png'),
                fit: BoxFit.contain,
              ),
            ),
            SizedBox(width: 10),
            Expanded(
              child: Text(
                'Admin',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.w800,
                  color: AppColors.brandBlue,
                ),
              ),
            ),
          ],
        ),
        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.search_outlined),
          ),
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.filter_list_rounded),
          ),
          IconButton(
            onPressed: () {
              Navigator.of(context).pushNamed(AppRouter.settings);
            },
            icon: const Icon(Icons.settings_outlined),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: SafeArea(
        top: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 22),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextField(
                decoration: InputDecoration(
                  hintText: 'Tìm theo tên, email...',
                  prefixIcon: const Icon(Icons.search_rounded),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: AppColors.border),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: AppColors.border),
                  ),
                ),
              ),
              const SizedBox(height: 12),
              const SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: [
                    _FilterChip(label: 'Tất cả', isActive: true),
                    SizedBox(width: 8),
                    _FilterChip(label: 'Admin'),
                    SizedBox(width: 8),
                    _FilterChip(label: 'Hoạt động'),
                    SizedBox(width: 8),
                    _FilterChip(label: 'Editor'),
                    SizedBox(width: 8),
                    _FilterChip(label: 'Viewer'),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Checkbox(
                    value: false,
                    onChanged: (_) {},
                  ),
                  Text(
                    'CHỌN TẤT CẢ (${_users.length})',
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      color: AppColors.textMuted,
                    ),
                  ),
                  const Spacer(),
                  const Icon(
                    Icons.storage_rounded,
                    size: 16,
                    color: AppColors.brandRed,
                  ),
                  const SizedBox(width: 4),
                  const Text(
                    'QUẢN LÝ DỮ LIỆU',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: AppColors.brandRed,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ..._users.map((user) => _UserCard(user: user)),
              const SizedBox(height: 12),
              ListTile(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                tileColor: Colors.white,
                title: const Text(
                  'Bộ lọc nâng cao',
                  style: TextStyle(fontWeight: FontWeight.w600),
                ),
                trailing: const Icon(Icons.keyboard_arrow_down_rounded),
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                  const Text('Số dòng:'),
                  const SizedBox(width: 10),
                  ...const [10, 20, 50].map(_RowCountChip.new),
                  const Spacer(),
                  const Text(
                    '1 - 4 TRÊN 150 NGƯỜI DÙNG',
                    style: TextStyle(
                      fontSize: 11,
                      color: AppColors.textMuted,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),
              const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _PaginationArrow(icon: Icons.chevron_left_rounded),
                  SizedBox(width: 10),
                  _PaginationNumber(label: '1', isActive: true),
                  SizedBox(width: 10),
                  _PaginationNumber(label: '2'),
                  SizedBox(width: 10),
                  _PaginationNumber(label: '3'),
                  SizedBox(width: 10),
                  Text('...'),
                  SizedBox(width: 10),
                  _PaginationNumber(label: '15'),
                  SizedBox(width: 10),
                  _PaginationArrow(icon: Icons.chevron_right_rounded),
                ],
              ),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {},
        backgroundColor: AppColors.brandRed,
        child: const Icon(Icons.person_add_alt_1_rounded),
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _bottomNavIndex,
        selectedItemColor: AppColors.brandRed,
        onTap: (index) {
          setState(() {
            _bottomNavIndex = index;
          });
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.groups_rounded),
            label: 'Người dùng',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.add_circle_outline_rounded),
            label: 'Tạo mới',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.inventory_2_outlined),
            label: 'Quản lý',
          ),
        ],
      ),
    );
  }
}

class _AdminUserItem {
  const _AdminUserItem({
    required this.name,
    required this.email,
    required this.role,
    required this.createdAt,
    required this.isActive,
  });

  final String name;
  final String email;
  final String role;
  final String createdAt;
  final bool isActive;
}

class _UserCard extends StatelessWidget {
  const _UserCard({required this.user});

  final _AdminUserItem user;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Checkbox(value: false, onChanged: (_) {}),
              const CircleAvatar(
                radius: 22,
                backgroundColor: Color(0xFFEAEFF6),
                child: Icon(Icons.person, color: AppColors.textMuted),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      user.name,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    Text(
                      user.email,
                      style: const TextStyle(color: AppColors.textMuted),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.more_vert_rounded, color: AppColors.textMuted),
            ],
          ),
          const Divider(height: 20),
          Row(
            children: [
              Expanded(
                child: _MetaColumn(
                  label: 'VAI TRÒ',
                  child: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: const Color(0xFFEEF3F8),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      user.role,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    ),
                  ),
                ),
              ),
              Expanded(
                child: _MetaColumn(
                  label: 'NGÀY TẠO',
                  alignEnd: true,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      const Icon(
                        Icons.access_time_rounded,
                        size: 14,
                        color: AppColors.textMuted,
                      ),
                      const SizedBox(width: 4),
                      Text(user.createdAt),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Switch(
                value: user.isActive,
                onChanged: (_) {},
                activeThumbColor: Colors.white,
                activeTrackColor: AppColors.brandRed,
              ),
              Text(user.isActive ? 'Đang hoạt động' : 'Tạm ngắt'),
              const Spacer(),
              OutlinedButton(
                onPressed: () {},
                child: const Text('Sửa nhanh'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _MetaColumn extends StatelessWidget {
  const _MetaColumn({
    required this.label,
    required this.child,
    this.alignEnd = false,
  });

  final String label;
  final Widget child;
  final bool alignEnd;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment:
          alignEnd ? CrossAxisAlignment.end : CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w700,
            color: AppColors.textMuted,
          ),
        ),
        const SizedBox(height: 6),
        child,
      ],
    );
  }
}

class _FilterChip extends StatelessWidget {
  const _FilterChip({required this.label, this.isActive = false});

  final String label;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: isActive ? AppColors.brandRed : Colors.white,
        borderRadius: BorderRadius.circular(999),
        border: Border.all(
          color: isActive ? AppColors.brandRed : AppColors.border,
        ),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w700,
          color: isActive ? Colors.white : AppColors.textPrimary,
        ),
      ),
    );
  }
}

class _RowCountChip extends StatelessWidget {
  const _RowCountChip(this.count);

  final int count;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(right: 6),
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: count == 10 ? AppColors.brandBlue : const Color(0xFFE9EDF2),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        '$count',
        style: TextStyle(
          fontSize: 11,
          color: count == 10 ? Colors.white : AppColors.textMuted,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _PaginationNumber extends StatelessWidget {
  const _PaginationNumber({required this.label, this.isActive = false});

  final String label;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 34,
      height: 34,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: isActive ? AppColors.brandBlue : Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppColors.border),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontWeight: FontWeight.w700,
          color: isActive ? Colors.white : AppColors.textPrimary,
        ),
      ),
    );
  }
}

class _PaginationArrow extends StatelessWidget {
  const _PaginationArrow({required this.icon});

  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 30,
      height: 30,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppColors.border),
      ),
      child: Icon(icon, color: AppColors.textMuted),
    );
  }
}
