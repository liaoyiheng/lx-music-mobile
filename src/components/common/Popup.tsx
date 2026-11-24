import { forwardRef, useImperativeHandle, useMemo, useRef } from 'react'
import { View, TouchableOpacity } from 'react-native'

import Modal, { type ModalType } from './Modal'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { IonIcon } from '@/components/common/Icon'
import { useKeyboard } from '@/utils/hooks'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'
import Text from './Text'
import { useStatusbarHeight } from '@/store/common/hook'

const styles = createStyle({
  centeredView: {
    flex: 1,
  },
  modalView: {
    flexGrow: 0,
    flexShrink: 1,
  },
  sideFill: {
    flex: 1,
  },
  header: {
    flex: 0,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  grabber: {
    position: 'absolute',
    top: 8,
    left: 0,
    right: 0,
    alignItems: 'center',
  },
  grabberBar: {
    width: 36,
    height: 5,
    borderRadius: 3,
  },
  title: {
    flex: 1,
    textAlign: 'center',
    fontWeight: fontWeight.semibold,
    paddingLeft: 52,
    paddingRight: 52,
    paddingTop: 22,
    paddingBottom: 12,
    letterSpacing: -0.2,
  },
  closeBtn: {
    position: 'absolute',
    flexGrow: 0,
    flexShrink: 0,
    top: 13,
    right: 14,
    height: 30,
    width: 30,
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
  headerDivider: {
    height: hairline,
  },
})

export interface PopupProps {
  onHide?: () => void
  keyHide?: boolean
  bgHide?: boolean
  closeBtn?: boolean
  position?: 'top' | 'left' | 'right' | 'bottom'
  title?: string
  children: React.ReactNode
}

export interface PopupType {
  setVisible: (visible: boolean) => void
}

export default forwardRef<PopupType, PopupProps>(({
  onHide = () => {},
  keyHide = true,
  bgHide = true,
  closeBtn = true,
  position = 'bottom',
  title = '',
  children,
}: PopupProps, ref) => {
  const apple = useApple()
  const { keyboardShown, keyboardHeight } = useKeyboard()
  const statusBarHeight = useStatusbarHeight()

  const modalRef = useRef<ModalType>(null)

  useImperativeHandle(ref, () => ({
    setVisible(visible: boolean) {
      modalRef.current?.setVisible(visible)
    },
  }))

  const closeBtnComponent = useMemo(() => closeBtn
    ? <TouchableOpacity style={styles.closeBtn} activeOpacity={0.65} onPress={() => modalRef.current?.setVisible(false)}>
        <View style={[styles.closeCircle, { backgroundColor: apple.fill }]}>
          <IonIcon name="close" color={apple.secondaryLabel} rawSize={17} />
        </View>
      </TouchableOpacity>
    : null, [closeBtn, apple.fill, apple.secondaryLabel])

  const [centeredViewStyle, modalViewStyle, glassRadius, edgeRadiusStyle] = useMemo(() => {
    switch (position) {
      case 'top':
        return [
          {
            position: 'absolute',
            left: 0,
            right: 0,
            bottom: 0,
            top: 0,
            justifyContent: 'flex-start',
          },
          {
            width: '100%',
            maxHeight: '78%',
            minHeight: '20%',
          },
          radius.glassSheet,
          {
            borderTopLeftRadius: 0,
            borderTopRightRadius: 0,
          },
        ] as const
      case 'left':
        return [
          {
            position: 'absolute',
            left: 0,
            right: 0,
            bottom: 0,
            top: 0,
            flexDirection: 'row',
            justifyContent: 'flex-start',
          },
          {
            minWidth: '45%',
            maxWidth: '78%',
            height: '100%',
          },
          0,
          {
            paddingTop: statusBarHeight,
          },
        ] as const
      case 'right':
        return [
          {
            position: 'absolute',
            left: 0,
            right: 0,
            bottom: 0,
            top: 0,
            flexDirection: 'row',
            justifyContent: 'flex-end',
          },
          {
            minWidth: '45%',
            maxWidth: '78%',
            height: '100%',
          },
          0,
          {
            paddingTop: statusBarHeight,
          },
        ] as const
      case 'bottom':
      default:
        return [
          {
            position: 'absolute',
            left: 0,
            right: 0,
            bottom: 0,
            top: 0,
            justifyContent: 'flex-end',
          },
          {
            width: '100%',
            maxHeight: '78%',
            minHeight: '20%',
          },
          radius.glassSheet,
          {
            borderBottomLeftRadius: 0,
            borderBottomRightRadius: 0,
          },
        ] as const
    }
  }, [position, statusBarHeight])

  const isSide = position == 'left' || position == 'right'

  return (
    <Modal onHide={onHide} keyHide={keyHide} bgHide={bgHide} bgColor={apple.mask} ref={modalRef}>
      <View style={{ ...styles.centeredView, ...centeredViewStyle, paddingBottom: keyboardShown ? keyboardHeight : 0 }}>
        <View style={[styles.modalView, modalViewStyle]} onStartShouldSetResponder={() => true}>
          <GlassSurface
            dense
            borderRadius={glassRadius}
            style={[isSide ? styles.sideFill : undefined, edgeRadiusStyle]}
            innerStyle={[isSide ? styles.sideFill : undefined, edgeRadiusStyle]}
          >
            <View style={styles.header}>
              {position == 'bottom'
                ? <View style={styles.grabber} pointerEvents="none"><View style={[styles.grabberBar, { backgroundColor: apple.tertiaryLabel }]} /></View>
                : null}
              <Text size={17} color={apple.label} style={styles.title} numberOfLines={1}>{title}</Text>
              {closeBtnComponent}
            </View>
            {title ? <View style={[styles.headerDivider, { backgroundColor: apple.separator }]} /> : null}
            {children}
          </GlassSurface>
        </View>
      </View>
    </Modal>
  )
})
