import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:nfc_manager/nfc_manager.dart';

import '../../app/theme/app_colors.dart';
import '../../core/widgets/app_notice_dialog.dart';

enum _ScanMode { qr, nfc }

class ScanPage extends StatefulWidget {
  const ScanPage({super.key});

  @override
  State<ScanPage> createState() => _ScanPageState();
}

class _ScanPageState extends State<ScanPage> with WidgetsBindingObserver {
  final MobileScannerController scannerController = MobileScannerController(
    formats: const [BarcodeFormat.qrCode],
    detectionSpeed: DetectionSpeed.noDuplicates,
  );

  _ScanMode selectedMode = _ScanMode.qr;
  bool torchEnabled = false;
  bool isZoomed = false;
  bool isHandlingScan = false;
  bool isNfcAvailable = false;
  bool isNfcChecking = true;
  bool isNfcSessionRunning = false;
  String? lastScanValue;
  String? lastNfcValue;

  bool get _nfcTemporarilyDisabledOnIos =>
      defaultTargetPlatform == TargetPlatform.iOS;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkNfcAvailability();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _stopNfcSession();
    scannerController.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (selectedMode != _ScanMode.qr) {
      return;
    }

    if (state == AppLifecycleState.resumed) {
      scannerController.start();
      return;
    }

    if (state == AppLifecycleState.inactive ||
        state == AppLifecycleState.hidden ||
        state == AppLifecycleState.paused) {
      scannerController.stop();
    }
  }

  Future<void> _checkNfcAvailability() async {
    if (_nfcTemporarilyDisabledOnIos) {
      if (!mounted) {
        return;
      }
      setState(() {
        isNfcAvailable = false;
        isNfcChecking = false;
      });
      return;
    }

    final availability = await NfcManager.instance.checkAvailability();
    if (!mounted) {
      return;
    }

    setState(() {
      isNfcAvailable = availability == NfcAvailability.enabled;
      isNfcChecking = false;
    });
  }

  Future<void> _setMode(_ScanMode mode) async {
    if (selectedMode == mode) {
      return;
    }

    if (mode == _ScanMode.nfc && _nfcTemporarilyDisabledOnIos) {
      await showDialog<void>(
        context: context,
        builder: (context) {
          return const AppNoticeDialog(
            title: 'NFC tạm tắt trên iPhone',
            message:
                'Bản build test bằng Personal Team đang tắt NFC để bạn chạy và thử QR trước. Khi cần test NFC thật, mình sẽ bật lại với tài khoản Apple Developer trả phí.',
          );
        },
      );
      return;
    }

    setState(() {
      selectedMode = mode;
    });

    if (mode == _ScanMode.qr) {
      await _stopNfcSession();
      scannerController.start();
      return;
    }

    await scannerController.stop();
    await _startNfcSession();
  }

  Future<void> _toggleTorch() async {
    await scannerController.toggleTorch();
    if (!mounted) {
      return;
    }
    setState(() {
      torchEnabled = !torchEnabled;
    });
  }

  Future<void> _toggleZoom() async {
    if (!mounted) {
      return;
    }
    setState(() {
      isZoomed = !isZoomed;
    });
  }

  Future<void> _startNfcSession() async {
    if (isNfcChecking) {
      await _checkNfcAvailability();
    }

    if (!isNfcAvailable || isNfcSessionRunning) {
      return;
    }

    setState(() {
      isNfcSessionRunning = true;
      lastNfcValue = null;
    });

    try {
      await NfcManager.instance.startSession(
        pollingOptions: {
          NfcPollingOption.iso14443,
          NfcPollingOption.iso15693,
          NfcPollingOption.iso18092,
        },
        alertMessageIos: 'Đưa điện thoại lại gần thẻ NFC để quét stamp.',
        onDiscovered: (tag) async {
          final payload = _describeNfcTag(tag);
          if (!mounted) {
            return;
          }

          setState(() {
            lastNfcValue = payload;
            isNfcSessionRunning = false;
          });

          await NfcManager.instance.stopSession(
            alertMessageIos: 'Đã đọc thẻ NFC thành công.',
          );
        },
      );
    } catch (_) {
      if (!mounted) {
        return;
      }
      setState(() {
        isNfcSessionRunning = false;
      });
    }
  }

  Future<void> _stopNfcSession() async {
    if (!isNfcSessionRunning) {
      return;
    }

    try {
      await NfcManager.instance.stopSession();
    } catch (_) {
      // Ignore plugin/session shutdown errors when leaving the screen.
    }

    if (!mounted) {
      return;
    }

    setState(() {
      isNfcSessionRunning = false;
    });
  }

  Future<void> _handleDetect(BarcodeCapture capture) async {
    if (selectedMode != _ScanMode.qr || isHandlingScan) {
      return;
    }

    final rawValue = capture.barcodes
        .map((barcode) => barcode.rawValue)
        .whereType<String>()
        .firstWhere(
          (value) => value.trim().isNotEmpty,
          orElse: () => '',
        );

    if (rawValue.isEmpty) {
      return;
    }

    setState(() {
      isHandlingScan = true;
      lastScanValue = rawValue;
    });

    await scannerController.stop();

    if (!mounted) {
      return;
    }

    await showModalBottomSheet<void>(
      context: context,
      backgroundColor: const Color(0xFF121826),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (context) {
        return SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(24, 20, 24, 28),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 46,
                  height: 4,
                  margin: const EdgeInsets.only(bottom: 20),
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.16),
                    borderRadius: BorderRadius.circular(999),
                  ),
                ),
                const Text(
                  'QR scan thành công',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 10),
                const Text(
                  'Mã vừa quét được:',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFFA5B0C5),
                  ),
                ),
                const SizedBox(height: 10),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: const Color(0xFF1B2332),
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(color: Colors.white12),
                  ),
                  child: Text(
                    rawValue,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(height: 18),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () => Navigator.of(context).pop(),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.brandBlue,
                      foregroundColor: Colors.white,
                      minimumSize: const Size.fromHeight(54),
                    ),
                    child: const Text('Tiếp tục quét'),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );

    if (!mounted) {
      return;
    }

    setState(() {
      isHandlingScan = false;
    });
    scannerController.start();
  }

  Future<void> _showHelp() async {
    await showDialog<void>(
      context: context,
      builder: (context) {
        return AppNoticeDialog(
          title: 'Hướng dẫn quét',
          message: selectedMode == _ScanMode.qr
              ? isZoomed
                  ? 'Đưa mã QR vào trong vùng camera lớn, giữ máy ổn định và đảm bảo đủ sáng.'
                  : 'Đưa mã QR vào trong vòng tròn, giữ máy ổn định và đảm bảo đủ sáng.'
              : 'Chạm mặt lưng điện thoại vào tag NFC, giữ yên vài giây đến khi hệ thống nhận thẻ.',
        );
      },
    );
  }

  String _describeNfcTag(NfcTag tag) {
    return tag.toString();
  }

  @override
  Widget build(BuildContext context) {
    final titleText = selectedMode == _ScanMode.qr
        ? 'Scan Stamp'
        : 'Tap NFC tag near the phone';
    final subtitleText = selectedMode == _ScanMode.qr
        ? lastScanValue == null
            ? 'Scan complete to unlock voucher'
            : 'Last QR: $lastScanValue'
        : lastNfcValue == null
            ? isNfcSessionRunning
                ? 'Waiting for NFC tag'
                : 'Tap to start NFC discovery'
            : lastNfcValue!;

    return Scaffold(
      backgroundColor: Colors.black,
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFF05070B),
              Color(0xFF030406),
              Color(0xFF090304),
            ],
          ),
        ),
        child: SafeArea(
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(18, 12, 18, 0),
                child: Row(
                  children: [
                    IconButton(
                      onPressed: () => Navigator.of(context).pop(),
                      icon: const Icon(
                        Icons.arrow_back_ios_new_rounded,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(width: 4),
                    const Expanded(
                      child: Text(
                        'Scan Stamp',
                        style: TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.w800,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    IconButton(
                      onPressed: _showHelp,
                      icon: const Icon(
                        Icons.help_outline_rounded,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
                  child: Column(
                    children: [
                      _SponsoredCard(
                        title: titleText,
                        subtitle: subtitleText,
                      ),
                      const SizedBox(height: 34),
                      _ScanPreviewPanel(
                        isExpanded: isZoomed,
                        selectedMode: selectedMode,
                        scannerController: scannerController,
                        torchEnabled: torchEnabled,
                        onDetect: _handleDetect,
                        onToggleTorch:
                            selectedMode == _ScanMode.qr ? _toggleTorch : null,
                        onToggleZoom:
                            selectedMode == _ScanMode.qr ? _toggleZoom : null,
                      ),
                      const SizedBox(height: 34),
                      _ScanModeSegmentedControl(
                        selectedMode: selectedMode,
                        onSelected: _setMode,
                        isNfcEnabled: !_nfcTemporarilyDisabledOnIos,
                      ),
                      const SizedBox(height: 28),
                      _ScanInfoCard(
                        title: selectedMode == _ScanMode.qr
                            ? isZoomed
                                ? 'Scan QR code anywhere in the large camera view'
                                : 'Align QR code within the circle'
                            : 'Hold device near the NFC tag',
                        description: selectedMode == _ScanMode.qr
                            ? isZoomed
                                ? 'Expanded mode scans the whole camera area, so you do not need to keep the QR code inside the circle.'
                                : 'Stamps are found near station ticket gates or information kiosks. Make sure you have an active internet connection.'
                            : isNfcAvailable
                                ? 'Use the back of your phone to read the NFC tag. Keep the device still until the app confirms the scan.'
                                : _nfcTemporarilyDisabledOnIos
                                    ? 'NFC is temporarily disabled on iPhone test builds signed with Personal Team. You can continue testing QR scanning.'
                                    : 'This device does not appear to support NFC. You can continue using QR scanning instead.',
                      ),
                      if (selectedMode == _ScanMode.nfc) ...[
                        const SizedBox(height: 18),
                        SizedBox(
                          width: double.infinity,
                          child: OutlinedButton(
                            onPressed: isNfcAvailable ? _startNfcSession : null,
                            style: OutlinedButton.styleFrom(
                              minimumSize: const Size.fromHeight(54),
                              side:
                                  const BorderSide(color: AppColors.brandBlue),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(18),
                              ),
                              foregroundColor: Colors.white,
                            ),
                            child: Text(
                              isNfcSessionRunning
                                  ? 'Scanning NFC...'
                                  : 'Start NFC scan',
                            ),
                          ),
                        ),
                      ],
                      const SizedBox(height: 30),
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(),
                        child: const Text(
                          'Cancel and Return',
                          style: TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.w700,
                            color: AppColors.brandRed,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              Container(
                height: 6,
                decoration: const BoxDecoration(
                  gradient: LinearGradient(
                    colors: [
                      Color(0xFF4AA6FF),
                      AppColors.brandRed,
                      Colors.white,
                    ],
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

class _ScanPreviewPanel extends StatelessWidget {
  const _ScanPreviewPanel({
    required this.isExpanded,
    required this.selectedMode,
    required this.scannerController,
    required this.torchEnabled,
    required this.onDetect,
    this.onToggleTorch,
    this.onToggleZoom,
  });

  final bool isExpanded;
  final _ScanMode selectedMode;
  final MobileScannerController scannerController;
  final bool torchEnabled;
  final void Function(BarcodeCapture capture) onDetect;
  final VoidCallback? onToggleTorch;
  final VoidCallback? onToggleZoom;

  @override
  Widget build(BuildContext context) {
    final previewSize = isExpanded ? 520.0 : 360.0;
    final scanAreaSize = isExpanded ? 500.0 : 276.0;
    final borderRadius = BorderRadius.circular(isExpanded ? 32 : 999);
    final outerBorderRadius = BorderRadius.circular(isExpanded ? 40 : 999);
    final helpIcon =
        isExpanded ? Icons.zoom_in_map_rounded : Icons.open_in_full_rounded;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 240),
      curve: Curves.easeOutCubic,
      width: double.infinity,
      height: previewSize,
      child: Stack(
        clipBehavior: Clip.none,
        alignment: Alignment.center,
        children: [
          AnimatedContainer(
            duration: const Duration(milliseconds: 240),
            curve: Curves.easeOutCubic,
            width: isExpanded ? double.infinity : 332,
            height: isExpanded ? previewSize - 24 : 332,
            decoration: BoxDecoration(
              borderRadius: outerBorderRadius,
              border: Border.all(
                color: AppColors.brandBlue.withValues(alpha: 0.22),
                width: 3,
              ),
              boxShadow: [
                BoxShadow(
                  color: AppColors.brandBlue.withValues(alpha: 0.12),
                  blurRadius: 24,
                  spreadRadius: 4,
                ),
              ],
            ),
          ),
          AnimatedContainer(
            duration: const Duration(milliseconds: 240),
            curve: Curves.easeOutCubic,
            width: isExpanded ? double.infinity : scanAreaSize,
            height: isExpanded ? scanAreaSize : scanAreaSize,
            decoration: BoxDecoration(
              borderRadius: borderRadius,
              border: Border.all(
                color: AppColors.brandBlue,
                width: 5,
              ),
            ),
            clipBehavior: Clip.antiAlias,
            child: DecoratedBox(
              decoration: const BoxDecoration(
                gradient: RadialGradient(
                  center: Alignment.center,
                  radius: 0.9,
                  colors: [
                    Color(0xFF0A0D14),
                    Color(0xFF06070C),
                  ],
                ),
              ),
              child: selectedMode == _ScanMode.qr
                  ? Stack(
                      fit: StackFit.expand,
                      children: [
                        MobileScanner(
                          controller: scannerController,
                          fit: BoxFit.cover,
                          onDetect: onDetect,
                        ),
                        _ScanFrameOverlay(isExpanded: isExpanded),
                        const Align(
                          alignment: Alignment.center,
                          child: _QrGuideMark(),
                        ),
                      ],
                    )
                  : const _NfcPreview(),
            ),
          ),
          Positioned(
            right: isExpanded ? 14 : 8,
            top: isExpanded ? 24 : 46,
            child: _CircleActionButton(
              icon: torchEnabled
                  ? Icons.flash_on_rounded
                  : Icons.flash_off_rounded,
              backgroundColor: AppColors.brandBlue,
              onTap: onToggleTorch,
            ),
          ),
          Positioned(
            left: isExpanded ? 14 : 12,
            bottom: isExpanded ? 20 : 30,
            child: _CircleActionButton(
              icon: helpIcon,
              backgroundColor: const Color(0xFF263049),
              onTap: onToggleZoom,
            ),
          ),
        ],
      ),
    );
  }
}

class _SponsoredCard extends StatelessWidget {
  const _SponsoredCard({
    required this.title,
    required this.subtitle,
  });

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 16),
      decoration: BoxDecoration(
        color: const Color(0xFF181D26),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white10),
      ),
      child: Column(
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 82,
                height: 82,
                decoration: BoxDecoration(
                  color: AppColors.brandBlue.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(22),
                  border: Border.all(
                    color: AppColors.brandBlue.withValues(alpha: 0.55),
                  ),
                ),
                child: const Icon(
                  Icons.local_cafe_outlined,
                  size: 36,
                  color: AppColors.brandBlue,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 10,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              borderRadius: BorderRadius.circular(999),
                              border: Border.all(
                                color:
                                    AppColors.brandBlue.withValues(alpha: 0.55),
                              ),
                            ),
                            child: const Text(
                              'SPONSORED',
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w800,
                                color: AppColors.brandBlue,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'Rewards in 3s',
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                            color: Color(0xFFA7B0C0),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      title,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      subtitle,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                        color: Color(0xFFA7B0C0),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              const Padding(
                padding: EdgeInsets.only(top: 26),
                child: Icon(
                  Icons.chevron_right_rounded,
                  size: 28,
                  color: Color(0xFF7F8AA1),
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: const LinearProgressIndicator(
              value: 0.22,
              minHeight: 5,
              valueColor: AlwaysStoppedAnimation(AppColors.brandBlue),
              backgroundColor: Colors.white,
            ),
          ),
        ],
      ),
    );
  }
}

class _ScanFrameOverlay extends StatelessWidget {
  const _ScanFrameOverlay({required this.isExpanded});

  final bool isExpanded;

  @override
  Widget build(BuildContext context) {
    final padding = isExpanded ? 38.0 : 54.0;

    return Stack(
      children: [
        _CornerMark(alignment: Alignment.topLeft, padding: padding),
        _CornerMark(alignment: Alignment.topRight, padding: padding),
        _CornerMark(alignment: Alignment.bottomLeft, padding: padding),
        _CornerMark(alignment: Alignment.bottomRight, padding: padding),
      ],
    );
  }
}

class _CornerMark extends StatelessWidget {
  const _CornerMark({
    required this.alignment,
    required this.padding,
  });

  final Alignment alignment;
  final double padding;

  @override
  Widget build(BuildContext context) {
    const cornerColor = Color(0xFFC64634);
    final isLeft = alignment.x < 0;
    final isTop = alignment.y < 0;

    return Align(
      alignment: alignment,
      child: Padding(
        padding: EdgeInsets.all(padding),
        child: SizedBox(
          width: 42,
          height: 42,
          child: CustomPaint(
            painter: _CornerPainter(
              color: cornerColor,
              isLeft: isLeft,
              isTop: isTop,
            ),
          ),
        ),
      ),
    );
  }
}

class _CornerPainter extends CustomPainter {
  const _CornerPainter({
    required this.color,
    required this.isLeft,
    required this.isTop,
  });

  final Color color;
  final bool isLeft;
  final bool isTop;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 4
      ..strokeCap = StrokeCap.square;

    final startX = isLeft ? 0.0 : size.width;
    final endX = isLeft ? size.width : 0.0;
    final startY = isTop ? 0.0 : size.height;
    final endY = isTop ? size.height : 0.0;

    canvas.drawLine(
      Offset(startX, startY),
      Offset(endX, startY),
      paint,
    );
    canvas.drawLine(
      Offset(startX, startY),
      Offset(startX, endY),
      paint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _QrGuideMark extends StatelessWidget {
  const _QrGuideMark();

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Icon(
          Icons.qr_code_2_rounded,
          size: 74,
          color: Colors.white24,
        ),
        Container(
          width: 126,
          height: 3,
          decoration: BoxDecoration(
            color: AppColors.brandBlue.withValues(alpha: 0.5),
            borderRadius: BorderRadius.circular(999),
          ),
        ),
      ],
    );
  }
}

class _NfcPreview extends StatelessWidget {
  const _NfcPreview();

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.nfc_rounded,
            size: 86,
            color: Colors.white24,
          ),
          SizedBox(height: 12),
          Text(
            'Hold near NFC tag',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: Color(0xFFB6C0D4),
            ),
          ),
        ],
      ),
    );
  }
}

class _CircleActionButton extends StatelessWidget {
  const _CircleActionButton({
    required this.icon,
    required this.backgroundColor,
    this.onTap,
  });

  final IconData icon;
  final Color backgroundColor;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(999),
        child: Ink(
          width: 64,
          height: 64,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: backgroundColor,
          ),
          child: Icon(
            icon,
            color: Colors.white,
            size: 30,
          ),
        ),
      ),
    );
  }
}

class _ScanModeSegmentedControl extends StatelessWidget {
  const _ScanModeSegmentedControl({
    required this.selectedMode,
    required this.onSelected,
    required this.isNfcEnabled,
  });

  final _ScanMode selectedMode;
  final ValueChanged<_ScanMode> onSelected;
  final bool isNfcEnabled;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: const Color(0xFF121725),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: const Color(0xFF28324A)),
      ),
      child: Row(
        children: [
          Expanded(
            child: _ScanModeButton(
              isActive: selectedMode == _ScanMode.qr,
              icon: Icons.qr_code_2_rounded,
              label: 'QR CODE',
              onTap: () => onSelected(_ScanMode.qr),
            ),
          ),
          Expanded(
            child: _ScanModeButton(
              isActive: selectedMode == _ScanMode.nfc,
              icon: Icons.nfc_rounded,
              label: 'NFC TAG',
              isEnabled: isNfcEnabled,
              onTap: () => onSelected(_ScanMode.nfc),
            ),
          ),
        ],
      ),
    );
  }
}

class _ScanModeButton extends StatelessWidget {
  const _ScanModeButton({
    required this.isActive,
    required this.icon,
    required this.label,
    required this.onTap,
    this.isEnabled = true,
  });

  final bool isActive;
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  final bool isEnabled;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: isEnabled ? onTap : null,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 16),
        decoration: BoxDecoration(
          color: isActive ? AppColors.brandBlue : Colors.transparent,
          borderRadius: BorderRadius.circular(999),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 24,
              color: isActive
                  ? Colors.white
                  : isEnabled
                      ? const Color(0xFF96A1B8)
                      : const Color(0xFF59647A),
            ),
            const SizedBox(width: 12),
            Text(
              label,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w800,
                color: isActive
                    ? Colors.white
                    : isEnabled
                        ? const Color(0xFF96A1B8)
                        : const Color(0xFF59647A),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ScanInfoCard extends StatelessWidget {
  const _ScanInfoCard({
    required this.title,
    required this.description,
  });

  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(18, 20, 18, 20),
      decoration: BoxDecoration(
        color: const Color(0xFF151112),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white10),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(color: AppColors.brandBlue, width: 2),
            ),
            child: const Icon(
              Icons.info_outline_rounded,
              size: 18,
              color: AppColors.brandBlue,
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  description,
                  style: const TextStyle(
                    fontSize: 14,
                    height: 1.65,
                    fontWeight: FontWeight.w500,
                    color: Color(0xFFAAB3C5),
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
