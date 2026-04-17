import 'dart:convert';

import 'package:flutter/material.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';
import '../../core/errors/app_exception.dart';
import '../../core/services/token_storage_service.dart';
import '../../core/widgets/app_notice_dialog.dart';
import '../auth/data/auth_api_service.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool autoUpdateEnabled = true;
  bool _isLoggingOut = false;
  bool _isLoadingProfile = true;
  CurrentUserProfile? _currentUser;
  final AuthApiService _authApiService = AuthApiService();
  final TokenStorageService _tokenStorageService = const TokenStorageService();

  @override
  void initState() {
    super.initState();
    _loadCurrentUser();
  }

  Future<void> _loadCurrentUser() async {
    String? authorizationHeader =
        await _tokenStorageService.getAuthorizationHeader();
    if (authorizationHeader == null || authorizationHeader.trim().isEmpty) {
      if (!mounted) {
        return;
      }
      setState(() {
        _isLoadingProfile = false;
      });
      return;
    }

    CurrentUserProfile? loadedUser;
    try {
      loadedUser = await _authApiService.getCurrentUser(
        authorizationHeader: authorizationHeader,
      );
    } on AppException {
      try {
        final refreshed = await _authApiService.refreshAccessToken();
        if (refreshed.accessToken != null &&
            refreshed.accessToken!.trim().isNotEmpty) {
          final tokenType = (refreshed.tokenType != null &&
                  refreshed.tokenType!.trim().isNotEmpty)
              ? refreshed.tokenType!
              : 'Bearer';
          await _tokenStorageService.saveToken(
            accessToken: refreshed.accessToken!,
            tokenType: tokenType,
          );

          authorizationHeader = '$tokenType ${refreshed.accessToken!}';
          loadedUser = await _authApiService.getCurrentUser(
            authorizationHeader: authorizationHeader,
          );
        }
      } catch (_) {
        // Keep fallback UI when profile API cannot be loaded.
      }
    } catch (_) {
      // Keep fallback UI when profile API cannot be loaded.
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _currentUser = loadedUser;
      _isLoadingProfile = false;
    });
  }

  Future<void> _handleLogout() async {
    if (_isLoggingOut) {
      return;
    }

    final shouldLogout = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          title: const Text(
            'Đăng xuất',
            style: TextStyle(fontWeight: FontWeight.w800),
          ),
          content: const Text(
            'Bạn có chắc muốn đăng xuất khỏi tài khoản hiện tại không?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('Hủy'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              child: const Text(
                'Đăng xuất',
                style: TextStyle(color: AppColors.brandRed),
              ),
            ),
          ],
        );
      },
    );

    if (shouldLogout != true || !mounted) {
      return;
    }

    setState(() {
      _isLoggingOut = true;
    });

    String? logoutError;
    String? authorizationHeader =
        await _tokenStorageService.getAuthorizationHeader();

    try {
      if (authorizationHeader != null) {
        await _authApiService.logout(
          authorizationHeader: authorizationHeader,
        );
      }
    } on AppException catch (exception) {
      final firstErrorMessage = exception.message;
      try {
        final refreshed = await _authApiService.refreshAccessToken();
        if (refreshed.accessToken != null &&
            refreshed.accessToken!.trim().isNotEmpty) {
          final tokenType = (refreshed.tokenType != null &&
                  refreshed.tokenType!.trim().isNotEmpty)
              ? refreshed.tokenType!
              : 'Bearer';

          await _tokenStorageService.saveToken(
            accessToken: refreshed.accessToken!,
            tokenType: tokenType,
          );

          authorizationHeader = '$tokenType ${refreshed.accessToken!}';
          await _authApiService.logout(
            authorizationHeader: authorizationHeader,
          );
        } else {
          logoutError = firstErrorMessage;
        }
      } on AppException catch (_) {
        logoutError = firstErrorMessage;
      } catch (_) {
        logoutError = firstErrorMessage;
      }
    } catch (_) {
      logoutError = 'Đăng xuất chưa thành công. Vui lòng thử lại.';
    } finally {
      await _tokenStorageService.clearToken();
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _isLoggingOut = false;
    });

    if (logoutError != null) {
      await showDialog<void>(
        context: context,
        builder: (context) => AppNoticeDialog(
          title: 'Phiên đăng nhập đã kết thúc',
          message: logoutError!,
        ),
      );

      if (!mounted) {
        return;
      }
    }

    Navigator.of(context).pushNamedAndRemoveUntil(
      AppRouter.auth,
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFDFDFE),
      appBar: AppBar(
        toolbarHeight: 72,
        leading: IconButton(
          onPressed: () => Navigator.of(context).pop(),
          icon: const Icon(Icons.arrow_back_ios_new_rounded, size: 20),
        ),
        title: const Text(
          'Cài đặt',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.w800,
            color: AppColors.textPrimary,
          ),
        ),
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(
            height: 1,
            color: const Color(0xFFE6EAF0),
          ),
        ),
      ),
      body: SafeArea(
        top: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(20, 28, 20, 40),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _SettingsProfileCard(
                user: _currentUser,
                isLoading: _isLoadingProfile,
                onTap: () {
                  Navigator.of(context).pushNamed(AppRouter.profile);
                },
              ),
              const SizedBox(height: 34),
              const _SettingsSectionTitle(title: 'TÀI KHOẢN'),
              const SizedBox(height: 12),
              const _SettingsGroup(
                children: [
                  _SettingsItem(
                    icon: Icons.lock_outline_rounded,
                    title: 'Thay đổi mật khẩu',
                    subtitle: 'Cập nhật bảo mật định kỳ',
                  ),
                  _SettingsItem(
                    icon: Icons.credit_card_rounded,
                    title: 'Gói thành viên',
                    trailingText: 'Premium',
                    trailingColor: AppColors.brandRed,
                  ),
                ],
              ),
              const SizedBox(height: 24),
              const _SettingsSectionTitle(title: 'ỨNG DỤNG'),
              const SizedBox(height: 12),
              _SettingsGroup(
                children: [
                  const _SettingsItem(
                    icon: Icons.notifications_none_rounded,
                    title: 'Thông báo',
                    subtitle: 'Quản lý âm thanh & biểu ngữ',
                  ),
                  const _SettingsItem(
                    icon: Icons.language_rounded,
                    title: 'Ngôn ngữ',
                    trailingText: 'Tiếng Việt',
                    trailingColor: AppColors.brandRed,
                  ),
                  _SettingsSwitchItem(
                    icon: Icons.sync_rounded,
                    title: 'Cập nhật tự động',
                    subtitle: 'Luôn sử dụng bản mới nhất',
                    value: autoUpdateEnabled,
                    onChanged: (value) {
                      setState(() {
                        autoUpdateEnabled = value;
                      });
                    },
                  ),
                ],
              ),
              const SizedBox(height: 24),
              const _SettingsSectionTitle(title: 'BẢO MẬT & HỖ TRỢ'),
              const SizedBox(height: 12),
              const _SettingsGroup(
                children: [
                  _SettingsItem(
                    icon: Icons.shield_outlined,
                    title: 'Quyền riêng tư',
                    subtitle: 'Quản lý dữ liệu & quyền truy cập',
                  ),
                  _SettingsItem(
                    icon: Icons.help_outline_rounded,
                    title: 'Trung tâm hỗ trợ',
                  ),
                ],
              ),
              const SizedBox(height: 48),
              _LogoutButton(
                onTap: _isLoggingOut ? null : _handleLogout,
                isLoading: _isLoggingOut,
              ),
              const SizedBox(height: 28),
              const Center(
                child: Text(
                  'PHIÊN BẢN 2.4.0',
                  style: TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 1.2,
                    color: Color(0xFF98A2B3),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SettingsProfileCard extends StatelessWidget {
  const _SettingsProfileCard({
    required this.user,
    required this.isLoading,
    this.onTap,
  });

  final CurrentUserProfile? user;
  final bool isLoading;
  final VoidCallback? onTap;

  Widget _buildAvatarContent() {
    final avatarUrl = user?.avatarUrl?.trim() ?? '';
    if (avatarUrl.isEmpty) {
      return const Icon(
        Icons.person_outline_rounded,
        size: 44,
        color: AppColors.background,
      );
    }

    if (avatarUrl.startsWith('data:image/')) {
      try {
        final base64Part = avatarUrl.split(',').last;
        final bytes = base64Decode(base64Part);
        return Image.memory(
          bytes,
          fit: BoxFit.cover,
          width: 88,
          height: 88,
        );
      } catch (_) {
        return const Icon(
          Icons.person_outline_rounded,
          size: 44,
          color: AppColors.background,
        );
      }
    }

    return Image.network(
      avatarUrl,
      fit: BoxFit.cover,
      width: 88,
      height: 88,
      errorBuilder: (_, __, ___) {
        return const Icon(
          Icons.person_outline_rounded,
          size: 44,
          color: AppColors.background,
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.fromLTRB(24, 22, 24, 22),
          decoration: BoxDecoration(
            color: const Color(0xFFF2F4FF),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: const Color(0xFFDDE3F6)),
          ),
          child: Row(
            children: [
              Stack(
                clipBehavior: Clip.none,
                children: [
                  Container(
                    width: 88,
                    height: 88,
                    decoration: const BoxDecoration(
                      color: AppColors.brandBlue,
                      shape: BoxShape.circle,
                    ),
                    child: ClipOval(
                      child: _buildAvatarContent(),
                    ),
                  ),
                  Positioned(
                    left: 38,
                    bottom: -2,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: AppColors.background,
                        borderRadius: BorderRadius.circular(999),
                        border: Border.all(
                          color: const Color(0xFFE3E7F0),
                          width: 2,
                        ),
                      ),
                      child: const Text(
                        'PRO',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w900,
                          color: AppColors.brandRed,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(width: 22),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      isLoading
                          ? 'Đang tải...'
                          : (user?.displayName ?? 'Người dùng'),
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      user?.email ?? 'Chưa có email',
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: AppColors.textMuted,
                      ),
                    ),
                    const SizedBox(height: 14),
                    const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(
                          'Chỉnh sửa hồ sơ',
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w700,
                            color: AppColors.brandBlue,
                          ),
                        ),
                        SizedBox(width: 6),
                        Icon(
                          Icons.chevron_right_rounded,
                          size: 18,
                          color: AppColors.brandBlue,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SettingsSectionTitle extends StatelessWidget {
  const _SettingsSectionTitle({required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.w900,
        letterSpacing: 0.2,
        color: Color(0xFF5D6678),
      ),
    );
  }
}

class _SettingsGroup extends StatelessWidget {
  const _SettingsGroup({required this.children});

  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: const Color(0xFFE8ECF2)),
        boxShadow: const [
          BoxShadow(
            color: Color(0x12000000),
            blurRadius: 16,
            offset: Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        children: [
          for (int index = 0; index < children.length; index++) ...[
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 18),
              child: children[index],
            ),
            if (index != children.length - 1)
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 18),
                child: Divider(height: 1, color: Color(0xFFE6EAF0)),
              ),
          ],
        ],
      ),
    );
  }
}

class _SettingsItem extends StatelessWidget {
  const _SettingsItem({
    required this.icon,
    required this.title,
    this.subtitle,
    this.trailingText,
    this.trailingColor = AppColors.textMuted,
  });

  final IconData icon;
  final String title;
  final String? subtitle;
  final String? trailingText;
  final Color trailingColor;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 84,
      child: Row(
        children: [
          _SettingsItemIcon(icon: icon),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
                if (subtitle != null) ...[
                  const SizedBox(height: 4),
                  Text(
                    subtitle!,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textMuted,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(width: 12),
          if (trailingText != null)
            Text(
              trailingText!,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: trailingColor,
              ),
            ),
          const SizedBox(width: 8),
          const Icon(
            Icons.chevron_right_rounded,
            size: 24,
            color: Color(0xFFB6BFCC),
          ),
        ],
      ),
    );
  }
}

class _SettingsSwitchItem extends StatelessWidget {
  const _SettingsSwitchItem({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 84,
      child: Row(
        children: [
          _SettingsItemIcon(icon: icon),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: AppColors.textMuted,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Switch.adaptive(
            value: value,
            activeTrackColor: AppColors.brandBlue,
            inactiveTrackColor: const Color(0xFFD0D5DD),
            inactiveThumbColor: AppColors.background,
            onChanged: onChanged,
          ),
        ],
      ),
    );
  }
}

class _SettingsItemIcon extends StatelessWidget {
  const _SettingsItemIcon({required this.icon});

  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 44,
      height: 44,
      decoration: BoxDecoration(
        color: const Color(0xFFF1F4FB),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Icon(
        icon,
        size: 24,
        color: AppColors.brandBlue,
      ),
    );
  }
}

class _LogoutButton extends StatelessWidget {
  const _LogoutButton({
    required this.onTap,
    this.isLoading = false,
  });

  final VoidCallback? onTap;
  final bool isLoading;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(22),
        child: Ink(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 20),
          decoration: BoxDecoration(
            color: const Color(0xFFFFF5F4),
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: const Color(0xFFF7DEDA)),
            boxShadow: const [
              BoxShadow(
                color: Color(0x0F000000),
                blurRadius: 12,
                offset: Offset(0, 5),
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: const Color(0xFFFDE3E0),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: const Icon(
                  Icons.logout_rounded,
                  color: AppColors.brandRed,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: isLoading
                    ? const Text(
                        'Đang đăng xuất...',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                          color: AppColors.brandRed,
                        ),
                      )
                    : const Text(
                        'Đăng xuất',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w800,
                          color: AppColors.brandRed,
                        ),
                      ),
              ),
              isLoading
                  ? const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.2,
                        color: AppColors.brandRed,
                      ),
                    )
                  : const Icon(
                      Icons.chevron_right_rounded,
                      size: 24,
                      color: Color(0xFFF28F82),
                    ),
            ],
          ),
        ),
      ),
    );
  }
}
