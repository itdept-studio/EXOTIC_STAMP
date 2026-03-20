import 'package:flutter/material.dart';

import '../../../app/router.dart';
import '../../../app/theme/app_colors.dart';

class ForgotPasswordPage extends StatefulWidget {
  const ForgotPasswordPage({super.key});

  @override
  State<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends State<ForgotPasswordPage> {
  bool useEmail = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 64,
        titleSpacing: 0,
        title: const Text(
          'Quên mật khẩu',
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: AppColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        top: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
          child: Column(
            children: [
              const _RecoverySteps(),
              const SizedBox(height: 14),
              Container(
                width: 80,
                height: 80,
                decoration: const BoxDecoration(
                  color: AppColors.brandRed,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.key_outlined,
                  size: 32,
                  color: AppColors.background,
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'Khôi phục tài khoản',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                'Nhập email hoặc số điện thoại đã đăng ký để nhận mã xác thực khôi phục quyền truy cập.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  height: 1.45,
                  color: AppColors.textMuted,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: const Color(0xFFF2F4F7),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: _ModeTab(
                        label: 'Email',
                        isActive: useEmail,
                        activeColor: AppColors.brandBlue,
                        onTap: () {
                          setState(() {
                            useEmail = true;
                          });
                        },
                      ),
                    ),
                    Expanded(
                      child: _ModeTab(
                        label: 'Số điện thoại',
                        isActive: !useEmail,
                        activeColor: AppColors.brandRed,
                        onTap: () {
                          setState(() {
                            useEmail = false;
                          });
                        },
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 22),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  useEmail ? 'ĐỊA CHỈ EMAIL' : 'SỐ ĐIỆN THOẠI',
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textMuted,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              _RecoveryInput(
                hintText: useEmail ? 'example@email.com' : '+84 901 234 567',
                prefixIcon:
                    useEmail ? Icons.mail_outline : Icons.phone_outlined,
              ),
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: () {},
                  child: const Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text('Gửi mã xác thực'),
                      SizedBox(width: 12),
                      Icon(Icons.arrow_forward_rounded),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 26),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text(
                    'Bạn đã nhớ mật khẩu? ',
                    style: TextStyle(
                      fontSize: 15,
                      color: AppColors.textMuted,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  GestureDetector(
                    onTap: () {
                      Navigator.of(context)
                          .pushReplacementNamed(AppRouter.auth);
                    },
                    child: const Text(
                      'Đăng nhập ngay',
                      style: TextStyle(
                        fontSize: 15,
                        color: AppColors.brandBlue,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 48),
              const Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      _BrandDot(),
                      SizedBox(width: 18),
                      _BrandDot(),
                      SizedBox(width: 18),
                      _BrandDot(),
                    ],
                  ),
                  SizedBox(height: 14),
                  Text.rich(
                    TextSpan(
                      children: [
                        TextSpan(
                          text: "EXOTIC'S ",
                          style: TextStyle(
                            color: Color(0xFFFF9D92),
                            letterSpacing: 2,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        TextSpan(
                          text: 'SECURITY SYSTEM',
                          style: TextStyle(
                            color: Color(0xFF98A2B3),
                            letterSpacing: 2,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RecoverySteps extends StatelessWidget {
  const _RecoverySteps();

  @override
  Widget build(BuildContext context) {
    return const Row(
      children: [
        Expanded(
          child: _RecoveryStep(
            step: '1',
            label: 'YÊU CẦU',
            isActive: true,
          ),
        ),
        Expanded(child: _RecoveryDivider()),
        Expanded(
          child: _RecoveryStep(
            step: '2',
            label: 'XÁC THỰC',
          ),
        ),
        Expanded(child: _RecoveryDivider()),
        Expanded(
          child: _RecoveryStep(
            step: '3',
            label: 'CẤP LẠI',
          ),
        ),
      ],
    );
  }
}

class _RecoveryStep extends StatelessWidget {
  const _RecoveryStep({
    required this.step,
    required this.label,
    this.isActive = false,
  });

  final String step;
  final String label;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 24,
          height: 24,
          decoration: BoxDecoration(
            color: isActive ? AppColors.brandBlue : const Color(0xFFF2F4F7),
            shape: BoxShape.circle,
            border: Border.all(
              color: isActive ? AppColors.brandBlue : AppColors.border,
            ),
          ),
          alignment: Alignment.center,
          child: Text(
            step,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: isActive ? AppColors.background : AppColors.textMuted,
            ),
          ),
        ),
        const SizedBox(height: 10),
        Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w700,
            color: isActive ? AppColors.brandBlue : AppColors.textMuted,
            letterSpacing: 0.4,
          ),
        ),
      ],
    );
  }
}

class _RecoveryDivider extends StatelessWidget {
  const _RecoveryDivider();

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 1.5,
      margin: const EdgeInsets.only(bottom: 28),
      color: AppColors.border,
    );
  }
}

class _ModeTab extends StatelessWidget {
  const _ModeTab({
    required this.label,
    required this.isActive,
    required this.activeColor,
    required this.onTap,
  });

  final String label;
  final bool isActive;
  final Color activeColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: isActive ? AppColors.background : Colors.transparent,
          borderRadius: BorderRadius.circular(14),
          boxShadow: isActive
              ? const [
                  BoxShadow(
                    color: AppColors.shadow,
                    blurRadius: 8,
                    offset: Offset(0, 3),
                  ),
                ]
              : null,
        ),
        child: Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            color: isActive ? activeColor : AppColors.textMuted,
          ),
        ),
      ),
    );
  }
}

class _RecoveryInput extends StatelessWidget {
  const _RecoveryInput({
    required this.hintText,
    required this.prefixIcon,
  });

  final String hintText;
  final IconData prefixIcon;

  @override
  Widget build(BuildContext context) {
    return TextField(
      decoration: InputDecoration(
        hintText: hintText,
        prefixIcon: Icon(
          prefixIcon,
          color: AppColors.brandBlue,
        ),
      ),
    );
  }
}

class _BrandDot extends StatelessWidget {
  const _BrandDot();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 10,
      height: 10,
      decoration: const BoxDecoration(
        color: Color(0xFF98BEE0),
        shape: BoxShape.circle,
      ),
    );
  }
}
