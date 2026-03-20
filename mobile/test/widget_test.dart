import 'package:flutter_test/flutter_test.dart';

import 'package:metro_stamp_app/app/app.dart';

void main() {
  testWidgets('shows the auth screen on startup', (WidgetTester tester) async {
    await tester.pumpWidget(const MetroStampApp());

    expect(find.text('Exotic Stamp'), findsOneWidget);
    expect(find.text('Chào mừng trở lại'), findsOneWidget);
    expect(find.text('Đăng nhập'), findsOneWidget);
  });
}
