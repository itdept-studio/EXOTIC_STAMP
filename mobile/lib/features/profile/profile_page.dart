import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';
import '../../core/errors/app_exception.dart';
import '../../core/services/token_storage_service.dart';
import '../../core/widgets/app_notice_dialog.dart';
import '../auth/data/auth_api_service.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  final AuthApiService _authApiService = AuthApiService();
  final TokenStorageService _tokenStorageService = const TokenStorageService();

  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _bioController = TextEditingController();
  final TextEditingController _avatarUrlController = TextEditingController();
  final TextEditingController _dobController = TextEditingController();
  final ImagePicker _imagePicker = ImagePicker();

  bool _isLoading = true;
  bool _isSaving = false;
  String? _error;
  CurrentUserProfile? _profile;
  bool _gender = true;
  Uint8List? _localAvatarBytes;
  String? _localAvatarDataUrl;

  @override
  void initState() {
    super.initState();
    _avatarUrlController.addListener(_onAvatarUrlChanged);
    _loadProfile();
  }

  @override
  void dispose() {
    _avatarUrlController.removeListener(_onAvatarUrlChanged);
    _firstNameController.dispose();
    _lastNameController.dispose();
    _bioController.dispose();
    _avatarUrlController.dispose();
    _dobController.dispose();
    super.dispose();
  }

  void _onAvatarUrlChanged() {
    if (mounted) {
      setState(() {});
    }
  }

  Future<String?> _currentAuthorizationHeader() async {
    final header = await _tokenStorageService.getAuthorizationHeader();
    if (header == null || header.trim().isEmpty) {
      return null;
    }
    return header;
  }

  Future<String?> _refreshAuthorizationHeader() async {
    final refreshed = await _authApiService.refreshAccessToken();
    if (refreshed.accessToken == null ||
        refreshed.accessToken!.trim().isEmpty) {
      return null;
    }

    final tokenType =
        (refreshed.tokenType != null && refreshed.tokenType!.trim().isNotEmpty)
            ? refreshed.tokenType!
            : 'Bearer';

    await _tokenStorageService.saveToken(
      accessToken: refreshed.accessToken!,
      tokenType: tokenType,
    );

    return '$tokenType ${refreshed.accessToken!}';
  }

  Future<void> _loadProfile() async {
    final authorizationHeader = await _currentAuthorizationHeader();
    if (authorizationHeader == null) {
      if (!mounted) {
        return;
      }
      setState(() {
        _isLoading = false;
        _error = 'Chưa có phiên đăng nhập hợp lệ.';
      });
      return;
    }

    CurrentUserProfile? loaded;
    String? error;

    try {
      loaded = await _authApiService.getCurrentUser(
        authorizationHeader: authorizationHeader,
      );
    } on AppException catch (exception) {
      error = exception.message;
      try {
        final refreshedHeader = await _refreshAuthorizationHeader();
        if (refreshedHeader != null) {
          loaded = await _authApiService.getCurrentUser(
            authorizationHeader: refreshedHeader,
          );
          error = null;
        }
      } on AppException catch (refreshException) {
        error = refreshException.message;
      } catch (_) {
        error = error ?? 'Không thể tải thông tin hồ sơ.';
      }
    } catch (_) {
      error = 'Không thể tải thông tin hồ sơ.';
    }

    if (!mounted) {
      return;
    }

    if (loaded != null) {
      _firstNameController.text = loaded.firstName ?? '';
      _lastNameController.text = loaded.lastName ?? '';
      _bioController.text = loaded.bio ?? '';
      _avatarUrlController.text = loaded.avatarUrl ?? '';
      _dobController.text = loaded.dob ?? '';
      _gender = loaded.gender ?? true;
    }

    setState(() {
      _isLoading = false;
      _profile = loaded;
      _error = error;
    });
  }

  Future<void> _showNotice({
    required String title,
    required String message,
  }) {
    return showDialog<void>(
      context: context,
      builder: (context) => AppNoticeDialog(
        title: title,
        message: message,
      ),
    );
  }

  Future<void> _pickAvatarFromLocal() async {
    final pickedFile = await _imagePicker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 85,
      maxWidth: 1200,
    );

    if (pickedFile == null) {
      return;
    }

    final bytes = await pickedFile.readAsBytes();
    if (!mounted) {
      return;
    }

    final extension = pickedFile.path.split('.').last.toLowerCase();
    String mimeType = 'image/jpeg';
    if (extension == 'png') {
      mimeType = 'image/png';
    } else if (extension == 'webp') {
      mimeType = 'image/webp';
    } else if (extension == 'gif') {
      mimeType = 'image/gif';
    }

    setState(() {
      _localAvatarBytes = bytes;
      _localAvatarDataUrl = 'data:$mimeType;base64,${base64Encode(bytes)}';
    });
  }

  void _clearLocalAvatar() {
    setState(() {
      _localAvatarBytes = null;
      _localAvatarDataUrl = null;
    });
  }

  ImageProvider? _buildAvatarProvider() {
    if (_localAvatarBytes != null) {
      return MemoryImage(_localAvatarBytes!);
    }

    final avatarUrl = _avatarUrlController.text.trim();
    if (avatarUrl.isNotEmpty) {
      return NetworkImage(avatarUrl);
    }

    final savedAvatar = _profile?.avatarUrl;
    if (savedAvatar != null && savedAvatar.trim().isNotEmpty) {
      return NetworkImage(savedAvatar.trim());
    }

    return null;
  }

  Future<void> _handleSave() async {
    if (_isSaving) {
      return;
    }

    final profile = _profile;
    if (profile == null || profile.id == null || profile.id!.trim().isEmpty) {
      await _showNotice(
        title: 'Thiếu định danh người dùng',
        message: 'Không tìm thấy user id để cập nhật hồ sơ.',
      );
      return;
    }

    final firstName = _firstNameController.text.trim();
    final lastName = _lastNameController.text.trim();
    final dob = _dobController.text.trim();

    if (firstName.isEmpty || lastName.isEmpty) {
      await _showNotice(
        title: 'Thiếu thông tin',
        message: 'Vui lòng nhập đầy đủ họ và tên.',
      );
      return;
    }

    if (dob.isNotEmpty && !RegExp(r'^\d{4}-\d{2}-\d{2}$').hasMatch(dob)) {
      await _showNotice(
        title: 'Ngày sinh chưa hợp lệ',
        message: 'Vui lòng dùng định dạng YYYY-MM-DD, ví dụ 2026-04-15.',
      );
      return;
    }

    var authorizationHeader = await _currentAuthorizationHeader();
    if (authorizationHeader == null) {
      await _showNotice(
        title: 'Phiên đăng nhập hết hạn',
        message: 'Vui lòng đăng nhập lại để tiếp tục.',
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    String? error;
    CurrentUserProfile? updated;

    try {
      updated = await _authApiService.updateUserProfile(
        authorizationHeader: authorizationHeader,
        userId: profile.id!,
        firstName: firstName,
        lastName: lastName,
        bio: _bioController.text.trim(),
        avatarUrl: _localAvatarDataUrl ?? _avatarUrlController.text.trim(),
        gender: _gender,
        dob: dob,
      );
    } on AppException catch (exception) {
      error = exception.message;
      try {
        final refreshedHeader = await _refreshAuthorizationHeader();
        if (refreshedHeader != null) {
          authorizationHeader = refreshedHeader;
          updated = await _authApiService.updateUserProfile(
            authorizationHeader: authorizationHeader,
            userId: profile.id!,
            firstName: firstName,
            lastName: lastName,
            bio: _bioController.text.trim(),
            avatarUrl: _localAvatarDataUrl ?? _avatarUrlController.text.trim(),
            gender: _gender,
            dob: dob,
          );
          error = null;
        }
      } on AppException catch (retryException) {
        error = retryException.message;
      } catch (_) {
        error = error ?? 'Cập nhật hồ sơ chưa thành công.';
      }
    } catch (_) {
      error = 'Cập nhật hồ sơ chưa thành công.';
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _isSaving = false;
      if (updated != null) {
        _profile = updated;
        _localAvatarBytes = null;
        _localAvatarDataUrl = null;
        _avatarUrlController.text = updated.avatarUrl ?? '';
      }
    });

    if (error != null) {
      await _showNotice(
        title: 'Không thể cập nhật',
        message: error,
      );
      return;
    }

    await _showNotice(
      title: 'Cập nhật thành công',
      message: 'Thông tin hồ sơ đã được cập nhật.',
    );

    if (!mounted) {
      return;
    }

    Navigator.of(context).pushNamedAndRemoveUntil(
      AppRouter.settings,
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Chỉnh sửa hồ sơ'),
      ),
      body: SafeArea(
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Text(
                        _error!,
                        textAlign: TextAlign.center,
                        style: const TextStyle(color: AppColors.brandRed),
                      ),
                    ),
                  )
                : ListView(
                    padding: const EdgeInsets.fromLTRB(16, 16, 16, 24),
                    children: [
                      _ProfileHeader(
                        profile: _profile,
                        avatarProvider: _buildAvatarProvider(),
                      ),
                      const SizedBox(height: 16),
                      _ReadOnlyTile(
                        label: 'Username',
                        value: _profile?.username,
                      ),
                      _ReadOnlyTile(
                        label: 'Email',
                        value: _profile?.email,
                      ),
                      _ReadOnlyTile(
                        label: 'Số điện thoại',
                        value: _profile?.phoneNumber,
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _firstNameController,
                        decoration: const InputDecoration(
                          labelText: 'First name',
                        ),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _lastNameController,
                        decoration: const InputDecoration(
                          labelText: 'Last name',
                        ),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _dobController,
                        decoration: const InputDecoration(
                          labelText: 'Ngày sinh (YYYY-MM-DD)',
                        ),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _avatarUrlController,
                        decoration: const InputDecoration(
                          labelText: 'Avatar URL',
                        ),
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: _pickAvatarFromLocal,
                              icon: const Icon(Icons.upload_file_rounded),
                              label: const Text('Chọn ảnh từ máy'),
                            ),
                          ),
                          const SizedBox(width: 10),
                          if (_localAvatarBytes != null)
                            TextButton(
                              onPressed: _clearLocalAvatar,
                              child: const Text('Bỏ ảnh'),
                            ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _bioController,
                        maxLines: 3,
                        decoration: const InputDecoration(
                          labelText: 'Bio',
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        children: [
                          const Text(
                            'Giới tính',
                            style: TextStyle(
                              fontWeight: FontWeight.w700,
                              color: AppColors.textPrimary,
                            ),
                          ),
                          const Spacer(),
                          Text(_gender ? 'Nam' : 'Nữ'),
                          Switch.adaptive(
                            value: _gender,
                            onChanged: (value) {
                              setState(() {
                                _gender = value;
                              });
                            },
                          ),
                        ],
                      ),
                      const SizedBox(height: 18),
                      SizedBox(
                        height: 50,
                        child: ElevatedButton(
                          onPressed: _isSaving ? null : _handleSave,
                          child: _isSaving
                              ? const SizedBox(
                                  width: 20,
                                  height: 20,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Text('Lưu thay đổi'),
                        ),
                      ),
                    ],
                  ),
      ),
    );
  }
}

class _ProfileHeader extends StatelessWidget {
  const _ProfileHeader({
    required this.profile,
    required this.avatarProvider,
  });

  final CurrentUserProfile? profile;
  final ImageProvider? avatarProvider;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFFF2F4FF),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: const Color(0xFFDDE3F6)),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 26,
            backgroundColor: AppColors.brandBlue,
            backgroundImage: avatarProvider,
            child: avatarProvider == null
                ? const Icon(
                    Icons.person_outline_rounded,
                    color: Colors.white,
                  )
                : null,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  profile?.displayName ?? 'Người dùng',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w800,
                    color: AppColors.textPrimary,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  profile?.email ?? 'Chưa có email',
                  style: const TextStyle(
                    color: AppColors.textMuted,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ReadOnlyTile extends StatelessWidget {
  const _ReadOnlyTile({
    required this.label,
    required this.value,
  });

  final String label;
  final String? value;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.border),
      ),
      child: Row(
        children: [
          Text(
            '$label: ',
            style: const TextStyle(
              color: AppColors.textMuted,
              fontWeight: FontWeight.w600,
            ),
          ),
          Expanded(
            child: Text(
              (value == null || value!.trim().isEmpty) ? '-' : value!,
              textAlign: TextAlign.right,
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
