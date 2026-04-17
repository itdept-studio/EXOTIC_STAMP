import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../app/router.dart';
import '../../../app/theme/app_colors.dart';
import '../../../core/errors/app_exception.dart';
import '../../../core/widgets/app_notice_dialog.dart';
import '../data/auth_api_service.dart';

class VerifyEmailOtpPage extends StatefulWidget {
  const VerifyEmailOtpPage({
    super.key,
    required this.email,
  });

  final String email;

  @override
  State<VerifyEmailOtpPage> createState() => _VerifyEmailOtpPageState();
}

class _VerifyEmailOtpPageState extends State<VerifyEmailOtpPage> {
  static const int _otpLength = 6;
  static const int _resendCooldownSeconds = 45;

  final AuthApiService _authApiService = AuthApiService();
  final List<TextEditingController> _otpControllers =
      List<TextEditingController>.generate(
    _otpLength,
    (_) => TextEditingController(),
  );
  final List<FocusNode> _focusNodes = List<FocusNode>.generate(
    _otpLength,
    (_) => FocusNode(),
  );

  Timer? _resendTimer;
  int _secondsLeft = _resendCooldownSeconds;
  bool _isVerifying = false;
  bool _isResending = false;
  String? _otpError;

  @override
  void initState() {
    super.initState();
    _startResendTimer();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        _focusNodes.first.requestFocus();
      }
    });
  }

  @override
  void dispose() {
    _resendTimer?.cancel();
    for (final controller in _otpControllers) {
      controller.dispose();
    }
    for (final focusNode in _focusNodes) {
      focusNode.dispose();
    }
    super.dispose();
  }

  bool get _isOtpComplete =>
      _otpControllers.every((controller) => controller.text.length == 1);

  String get _otpCode =>
      _otpControllers.map((controller) => controller.text).join();

  void _startResendTimer() {
    _resendTimer?.cancel();
    setState(() {
      _secondsLeft = _resendCooldownSeconds;
    });

    _resendTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!mounted) {
        timer.cancel();
        return;
      }

      if (_secondsLeft <= 1) {
        timer.cancel();
        setState(() {
          _secondsLeft = 0;
        });
        return;
      }

      setState(() {
        _secondsLeft -= 1;
      });
    });
  }

  String _maskedEmail(String email) {
    final trimmed = email.trim();
    final atIndex = trimmed.indexOf('@');
    if (atIndex <= 1 || atIndex == trimmed.length - 1) {
      return trimmed;
    }

    final localPart = trimmed.substring(0, atIndex);
    final domainPart = trimmed.substring(atIndex);
    if (localPart.length <= 4) {
      return '${localPart[0]}***$domainPart';
    }

    final start = localPart.substring(0, 2);
    final end = localPart.substring(localPart.length - 2);
    return '$start***$end$domainPart';
  }

  void _onOtpChanged(int index, String value) {
    if (value.isNotEmpty) {
      _otpControllers[index].text = value[value.length - 1];
      _otpControllers[index].selection =
          const TextSelection.collapsed(offset: 1);
      if (index < _otpLength - 1) {
        _focusNodes[index + 1].requestFocus();
      } else {
        _focusNodes[index].unfocus();
      }
    } else if (index > 0) {
      _focusNodes[index - 1].requestFocus();
    }

    if (_otpError != null) {
      setState(() {
        _otpError = null;
      });
    } else {
      setState(() {});
    }
  }

  Future<void> _showNotice({
    required String title,
    required String message,
  }) {
    return showDialog<void>(
      context: context,
      builder: (context) {
        return AppNoticeDialog(title: title, message: message);
      },
    );
  }

  Future<void> _handleVerifyOtp() async {
    if (_isVerifying) {
      return;
    }

    if (!_isOtpComplete) {
      setState(() {
        _otpError = 'Vui lòng nhập đủ 6 chữ số OTP.';
      });
      return;
    }

    setState(() {
      _isVerifying = true;
      _otpError = null;
    });

    String? errorMessage;
    try {
      await _authApiService.verifyEmail(
        email: widget.email,
        otp: _otpCode,
      );
    } on AppException catch (exception) {
      errorMessage = exception.message;
    } catch (_) {
      errorMessage = 'Mã OTP không hợp lệ. Vui lòng kiểm tra lại.';
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _isVerifying = false;
    });

    if (errorMessage != null) {
      setState(() {
        _otpError = errorMessage;
      });
      return;
    }

    await _showNotice(
      title: 'Xác thực thành công',
      message: 'Tài khoản của bạn đã được kích hoạt. Vui lòng đăng nhập.',
    );

    if (!mounted) {
      return;
    }

    Navigator.of(context).pushNamedAndRemoveUntil(
      AppRouter.auth,
      (route) => false,
    );
  }

  Future<void> _handleResendOtp() async {
    if (_isResending || _secondsLeft > 0) {
      return;
    }

    setState(() {
      _isResending = true;
      _otpError = null;
    });

    String? errorMessage;
    try {
      await _authApiService.resendVerification(email: widget.email);
    } on AppException catch (exception) {
      errorMessage = exception.message;
    } catch (_) {
      errorMessage = 'Gửi lại mã chưa thành công. Vui lòng thử lại.';
    }

    if (!mounted) {
      return;
    }

    setState(() {
      _isResending = false;
    });

    if (errorMessage != null) {
      setState(() {
        _otpError = errorMessage;
      });
      return;
    }

    _startResendTimer();
    await _showNotice(
      title: 'Đã gửi lại mã',
      message: 'Vui lòng kiểm tra email để lấy mã OTP mới.',
    );
  }

  @override
  Widget build(BuildContext context) {
    final canResend = _secondsLeft == 0 && !_isResending;

    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 68,
        leading: IconButton(
          onPressed: () => Navigator.of(context).pop(),
          icon: const Icon(Icons.arrow_back_rounded),
        ),
        centerTitle: true,
        title: const Text.rich(
          TextSpan(
            children: [
              TextSpan(
                text: 'Exotic ',
                style: TextStyle(
                  color: AppColors.brandBlue,
                  fontSize: 34,
                  fontWeight: FontWeight.w700,
                ),
              ),
              TextSpan(
                text: 'Stamp',
                style: TextStyle(
                  color: AppColors.brandRed,
                  fontSize: 34,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
        ),
      ),
      body: SafeArea(
        top: false,
        child: Column(
          children: [
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(24, 30, 24, 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Container(
                      width: 98,
                      height: 98,
                      decoration: const BoxDecoration(
                        color: Color(0xFFFDEEEE),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.verified_user_outlined,
                        size: 46,
                        color: AppColors.brandRed,
                      ),
                    ),
                    const SizedBox(height: 24),
                    const Text(
                      'Nhập mã xác thực',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 38,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Text(
                      'Vui lòng nhập mã OTP gồm 6 chữ số đã gửi đến email ${_maskedEmail(widget.email)}',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                        fontSize: 16,
                        height: 1.45,
                        color: AppColors.textMuted,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 24),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: List<Widget>.generate(_otpLength, (index) {
                        return SizedBox(
                          width: 48,
                          child: TextField(
                            controller: _otpControllers[index],
                            focusNode: _focusNodes[index],
                            autofocus: index == 0,
                            textAlign: TextAlign.center,
                            keyboardType: TextInputType.number,
                            style: const TextStyle(
                              fontSize: 28,
                              fontWeight: FontWeight.w700,
                              color: AppColors.brandBlue,
                            ),
                            inputFormatters: [
                              FilteringTextInputFormatter.digitsOnly,
                              LengthLimitingTextInputFormatter(1),
                            ],
                            onChanged: (value) => _onOtpChanged(index, value),
                            decoration: InputDecoration(
                              contentPadding:
                                  const EdgeInsets.symmetric(vertical: 14),
                              isDense: true,
                              filled: true,
                              fillColor: AppColors.background,
                              enabledBorder: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(14),
                                borderSide: const BorderSide(
                                  color: AppColors.brandRed,
                                  width: 1.6,
                                ),
                              ),
                              focusedBorder: OutlineInputBorder(
                                borderRadius: BorderRadius.circular(14),
                                borderSide: const BorderSide(
                                  color: AppColors.brandRed,
                                  width: 2,
                                ),
                              ),
                            ),
                          ),
                        );
                      }),
                    ),
                    const SizedBox(height: 18),
                    if (_otpError != null)
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(
                            Icons.error_outline_rounded,
                            color: AppColors.brandRed,
                            size: 18,
                          ),
                          const SizedBox(width: 8),
                          Flexible(
                            child: Text(
                              _otpError!,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                color: AppColors.brandRed,
                                fontSize: 15,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                    if (_otpError != null) const SizedBox(height: 16),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 8,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF3F4F7),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        _secondsLeft > 0
                            ? 'Gửi lại mã sau ${_secondsLeft}s'
                            : 'Bạn có thể gửi lại mã',
                        style: const TextStyle(
                          color: AppColors.textMuted,
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: canResend ? _handleResendOtp : null,
                      child: _isResending
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text(
                              'Gửi lại mã',
                              style: TextStyle(fontWeight: FontWeight.w700),
                            ),
                    ),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 12, 24, 22),
              child: Column(
                children: [
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: ElevatedButton(
                      onPressed: _isVerifying ? null : _handleVerifyOtp,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.brandRed,
                        foregroundColor: AppColors.background,
                        disabledBackgroundColor: const Color(0xFFF5B4AD),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        textStyle: const TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      child: _isVerifying
                          ? const SizedBox(
                              width: 22,
                              height: 22,
                              child: CircularProgressIndicator(
                                strokeWidth: 2.2,
                                color: AppColors.background,
                              ),
                            )
                          : const Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text('Xác nhận'),
                                SizedBox(width: 12),
                                Icon(Icons.arrow_forward_ios_rounded, size: 16),
                              ],
                            ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Text.rich(
                    TextSpan(
                      style: TextStyle(
                        fontSize: 14,
                        color: AppColors.textMuted,
                        fontWeight: FontWeight.w500,
                        height: 1.45,
                      ),
                      children: [
                        TextSpan(
                            text: 'Bằng việc xác nhận, bạn đồng ý với các '),
                        TextSpan(
                          text: 'Điều khoản & Điều kiện',
                          style: TextStyle(
                            color: AppColors.textPrimary,
                            fontWeight: FontWeight.w700,
                            decoration: TextDecoration.underline,
                          ),
                        ),
                        TextSpan(text: ' của chúng tôi.'),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
