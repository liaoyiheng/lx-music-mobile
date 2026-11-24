import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { LIST_IDS } from '@/config/constant'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'

const getListIcon = (id: string) => {
  switch (id) {
    case LIST_IDS.LOVE: return 'heart'
    case LIST_IDS.DEFAULT: return 'musical-notes'
    default: return 'albums'
  }
}

export default ({ listInfo, onPress }: {
  listInfo: LX.List.MyListInfo
  onPress: (listInfo: LX.List.MyListInfo) => void
  width?: number
}) => {
  const apple = useApple()

  return (
    <TouchableOpacity
      activeOpacity={0.65}
      style={[styles.row, { backgroundColor: apple.fill }]}
      onPress={() => { onPress(listInfo) }}
    >
      <View
        style={[
          styles.iconBox,
          {
            backgroundColor: apple.accentFill,
            borderColor: apple.coverRing,
          },
        ]}
      >
        <IonIcon name={getListIcon(listInfo.id)} rawSize={18} color={apple.accent} />
      </View>
      <Text style={styles.name} numberOfLines={1} size={15} color={apple.label}>
        {listInfo.name}
      </Text>
      <IonIcon name="add-circle-outline" rawSize={20} color={apple.secondaryLabel} />
    </TouchableOpacity>
  )
}

export const styles = createStyle({
  row: {
    minHeight: 54,
    paddingHorizontal: 12,
    paddingVertical: 9,
    borderRadius: radius.large,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  iconBox: {
    width: 36,
    height: 36,
    borderRadius: 9,
    borderWidth: hairline,
    alignItems: 'center',
    justifyContent: 'center',
  },
  name: {
    flex: 1,
    fontWeight: fontWeight.medium,
  },
})
