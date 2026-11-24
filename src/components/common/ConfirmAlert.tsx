import { forwardRef, useImperativeHandle, useRef } from 'react'
import { View, ScrollView } from 'react-native'
import Dialog, { type DialogType } from './Dialog'
import Button from './Button'
import { createStyle } from '@/utils/tools'
import { useI18n } from '@/lang/index'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'
import Text from './Text'

const styles = createStyle({
  main: {
    flexShrink: 1,
    paddingHorizontal: 18,
    paddingTop: 2,
    paddingBottom: 18,
  },
  content: {
    flexGrow: 0,
  },
  messageText: {
    textAlign: 'center',
    lineHeight: 20,
  },
  btns: {
    flexDirection: 'row',
    gap: 10,
    paddingHorizontal: 18,
    paddingBottom: 18,
    paddingTop: 2,
  },
  btnsReversedDirection: {
    flexDirection: 'row-reverse',
  },
  btn: {
    flex: 1,
    height: 44,
    borderRadius: radius.pill,
    borderWidth: hairline,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 12,
  },
  confirmBtn: {
    shadowOpacity: 0.3,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  btnDisabled: {
    opacity: 0.45,
  },
  btnText: {
    fontWeight: fontWeight.semibold,
  },
})

export interface ConfirmAlertProps {
  onCancel?: () => void
  onHide?: () => void
  onConfirm?: () => void
  keyHide?: boolean
  bgHide?: boolean
  closeBtn?: boolean
  title?: string
  text?: string
  cancelText?: string
  confirmText?: string
  showConfirm?: boolean
  disabledConfirm?: boolean
  reverseBtn?: boolean
  children?: React.ReactNode | React.ReactNode[]
}

export interface ConfirmAlertType {
  setVisible: (visible: boolean) => void
}

export default forwardRef<ConfirmAlertType, ConfirmAlertProps>(({
  onHide,
  onCancel,
  onConfirm = () => {},
  keyHide,
  bgHide,
  closeBtn,
  title = '',
  text = '',
  cancelText = '',
  confirmText = '',
  showConfirm = true,
  disabledConfirm = false,
  children,
  reverseBtn = false,
}: ConfirmAlertProps, ref) => {
  const apple = useApple()
  const t = useI18n()

  const dialogRef = useRef<DialogType>(null)

  useImperativeHandle(ref, () => ({
    setVisible(visible: boolean) {
      dialogRef.current?.setVisible(visible)
    },
  }))

  const handleCancel = () => {
    onCancel?.()
    dialogRef.current?.setVisible(false)
  }

  return (
    <Dialog onHide={onHide} keyHide={keyHide} bgHide={bgHide} closeBtn={closeBtn} title={title} ref={dialogRef}>
      <View style={styles.main}>
        <ScrollView style={styles.content} keyboardShouldPersistTaps={'always'}>
          {children ?? <Text size={14} color={apple.secondaryLabel} style={styles.messageText}>{text}</Text>}
        </ScrollView>
      </View>
      <View style={[styles.btns, reverseBtn ? styles.btnsReversedDirection : null]}>
        <Button
          style={[
            styles.btn,
            {
              backgroundColor: apple.fill,
              borderColor: apple.glassBorder,
            },
          ]}
          activeOpacity={0.65}
          onPress={handleCancel}
        >
          <Text size={15} color={apple.label} style={styles.btnText} numberOfLines={1}>
            {cancelText || t('cancel')}
          </Text>
        </Button>
        {showConfirm
          ? (
              <Button
                style={[
                  styles.btn,
                  styles.confirmBtn,
                  {
                    backgroundColor: apple.accent,
                    borderColor: 'rgba(255, 255, 255, 0.34)',
                    shadowColor: apple.accent,
                  },
                  disabledConfirm ? styles.btnDisabled : null,
                ]}
                activeOpacity={0.75}
                onPress={onConfirm}
                disabled={disabledConfirm}
              >
                <Text size={15} color={apple.onAccent} style={styles.btnText} numberOfLines={1}>
                  {confirmText || t('confirm')}
                </Text>
              </Button>
            )
          : null}
      </View>
    </Dialog>
  )
})
