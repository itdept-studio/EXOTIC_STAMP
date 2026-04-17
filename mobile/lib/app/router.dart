import 'package:flutter/material.dart';

import '../features/admin/admin_user_management_page.dart';
import '../features/auth/presentation/auth_page.dart';
import '../features/auth/presentation/forgot_password_page.dart';
import '../features/auth/presentation/forgot_password_otp_page.dart';
import '../features/auth/presentation/register_page.dart';
import '../features/auth/presentation/verify_email_otp_page.dart';
import '../features/home/home_page.dart';
import '../features/profile/profile_page.dart';
import '../features/rewards/rewards_page.dart';
import '../features/scan/scan_page.dart';
import '../features/settings/settings_page.dart';
import '../features/stamp_book/stamp_book_page.dart';
import '../features/station/station_page.dart';

class AppRouter {
  static const home = '/home';
  static const auth = '/auth';
  static const adminUsers = '/admin-users';
  static const register = '/register';
  static const verifyEmailOtp = '/verify-email-otp';
  static const forgotPassword = '/forgot-password';
  static const forgotPasswordOtp = '/forgot-password-otp';
  static const stations = '/stations';
  static const scan = '/scan';
  static const stampBook = '/stamp-book';
  static const rewards = '/rewards';
  static const profile = '/profile';
  static const settings = '/settings';

  static Route<dynamic> onGenerateRoute(RouteSettings routeSettings) {
    switch (routeSettings.name) {
      case auth:
        return MaterialPageRoute(builder: (_) => const AuthPage());
      case adminUsers:
        return MaterialPageRoute(
            builder: (_) => const AdminUserManagementPage());
      case register:
        return MaterialPageRoute(builder: (_) => const RegisterPage());
      case verifyEmailOtp:
        final email = routeSettings.arguments is String
            ? routeSettings.arguments! as String
            : '';
        return MaterialPageRoute(
          builder: (_) => VerifyEmailOtpPage(email: email),
        );
      case forgotPassword:
        return MaterialPageRoute(builder: (_) => const ForgotPasswordPage());
      case forgotPasswordOtp:
        final email = routeSettings.arguments is String
            ? routeSettings.arguments! as String
            : '';
        return MaterialPageRoute(
          builder: (_) => ForgotPasswordOtpPage(email: email),
        );
      case stations:
        return MaterialPageRoute(builder: (_) => const StationPage());
      case scan:
        return MaterialPageRoute(builder: (_) => const ScanPage());
      case stampBook:
        return MaterialPageRoute(builder: (_) => const StampBookPage());
      case rewards:
        return MaterialPageRoute(builder: (_) => const RewardsPage());
      case profile:
        return MaterialPageRoute(builder: (_) => const ProfilePage());
      case settings:
        return MaterialPageRoute(builder: (_) => const SettingsPage());
      case home:
        return MaterialPageRoute(builder: (_) => const HomePage());
      default:
        return MaterialPageRoute(builder: (_) => const AuthPage());
    }
  }
}
