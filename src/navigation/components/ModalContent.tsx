import { View } from 'react-native'
import { GlassSurface } from '@/components/apple/LiquidGlass'
import { useApple } from '@/theme/apple'
import { createStyle } from '@/utils/tools'

const MODAL_RADIUS = 22

interface Props {
  children: React.ReactNode
}

export default ({ children }: Props) => {
  const apple = useApple()

  return (
    <View style={[styles.centeredView, { backgroundColor: apple.mask }]}>
      <View style={styles.modalView}>
        <GlassSurface dense borderRadius={MODAL_RADIUS}>
          {children}
        </GlassSurface>
      </View>
    </View>
  )
}

const styles = createStyle({
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  modalView: {
    width: '100%',
    maxWidth: 400,
    maxHeight: '78%',
  },
})
