import { useImperativeHandle, forwardRef, useMemo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'

import Modal, { type ModalType } from './Modal'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight } from '@/theme/apple'
import Text from './Text'
import { scaleSizeH } from '@/utils/pixelRatio'

const HEADER_HEIGHT = 48
const DIALOG_RADIUS = 22

const styles = createStyle({
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalView: {
    maxWidth: '88%',
    minWidth: '72%',
    maxHeight: '78%',
  },
  fillHeight: {
    flex: 1,
  },
  header: {
    flexGrow: 0,
    flexShrink: 0,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: HEADER_HEIGHT,
    paddingTop: 4,
  },
  compactHeader: {
    minHeight: 16,
  },
  title: {
    paddingLeft: 44,
    paddingRight: 44,
    textAlign: 'center',
    fontWeight: fontWeight.bold,
    flexShrink: 1,
    letterSpacing: -0.2,
  },
  closeBtn: {
    position: 'absolute',
    right: 6,
    top: 4,
    flexGrow: 0,
    flexShrink: 0,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeCircle: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
})

export interface DialogProps {
  onHide?: () => void
  keyHide?: boolean
  bgHide?: boolean
  closeBtn?: boolean
  title?: string
  children: React.ReactNode | React.ReactNode[]
  height?: number | `${number}%`
}

export interface DialogType {
  setVisible: (visible: boolean) => void
}

export default forwardRef<DialogType, DialogProps>(({
  onHide,
  keyHide = true,
  bgHide = true,
  closeBtn = true,
  title = '',
  children,
  height,
}: DialogProps, ref) => {
  const apple = useApple()
  const { keyboardShown, keyboardHeight } = useKeyboard()
  const modalRef = useRef<ModalType>(null)

  useImperativeHandle(ref, () => ({
    setVisible(visible: boolean) {
      modalRef.current?.setVisible(visible)
    },
  }))

  const closeBtnComponent = useMemo(() => {
    return closeBtn
      ? <TouchableOpacity style={{ ...styles.closeBtn, width: scaleSizeH(40) }} activeOpacity={0.65} onPress={() => modalRef.current?.setVisible(false)}>
          <View style={[styles.closeCircle, { backgroundColor: apple.fill }]}>
            <IonIcon name="close" color={apple.secondaryLabel} rawSize={16} />
          </View>
        </TouchableOpacity>
      : null
  }, [closeBtn, apple.fill, apple.secondaryLabel])

  const hasHeader = Boolean(title) || closeBtn

  return (
    <Modal onHide={onHide} keyHide={keyHide} bgHide={bgHide} bgColor={apple.mask} ref={modalRef}>
      <View style={{ ...styles.centeredView, paddingBottom: keyboardShown ? keyboardHeight : 0 }}>
        <View
          style={[styles.modalView, height != null ? { height } : null]}
          onStartShouldSetResponder={() => true}
        >
          <GlassSurface
            dense
            borderRadius={DIALOG_RADIUS}
            style={height != null ? styles.fillHeight : undefined}
            innerStyle={height != null ? styles.fillHeight : undefined}
          >
            {hasHeader ? (
              <View style={styles.header}>
                <Text style={styles.title} size={17} color={apple.label} numberOfLines={1}>{title}</Text>
                {closeBtnComponent}
              </View>
            ) : <View style={styles.compactHeader} />}
            {children}
          </GlassSurface>
        </View>
      </View>
    </Modal>
  )
})
