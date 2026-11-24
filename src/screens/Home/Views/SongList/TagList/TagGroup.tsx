import { TouchableOpacity, View } from 'react-native'
import { type TagInfoItem } from '@/store/songlist/state'
import { createStyle } from '@/utils/tools'
import Text from '@/components/common/Text'
import { useApple, fontSize, fontWeight, radius, spacing } from '@/theme/apple'

export interface TagGroupProps {
  name: string
  list: TagInfoItem[]
  onTagChange: (name: string, id: string) => void
  activeId: string
}

export default ({ name, list, onTagChange, activeId }: TagGroupProps) => {
  const apple = useApple()
  return (
    <View style={styles.group}>
      {
        name
          ? <Text style={styles.tagTypeTitle} size={fontSize.subhead} color={apple.secondaryLabel}>{name}</Text>
          : null
      }
      <View style={styles.tagTypeList}>
        {list.map(item => {
          const active = activeId == item.id
          return (
            <TouchableOpacity
              key={item.id}
              activeOpacity={0.7}
              style={{
                ...styles.tagButton,
                backgroundColor: active ? apple.accent : apple.fill,
              }}
              onPress={() => { if (!active) onTagChange(item.name, item.id) }}
            >
              <Text
                style={{ fontWeight: active ? fontWeight.semibold : fontWeight.medium }}
                size={13.5}
                color={active ? apple.onAccent : apple.label}
              >
                {item.name}
              </Text>
            </TouchableOpacity>
          )
        })}
      </View>
    </View>
  )
}

const styles = createStyle({
  group: {
    marginBottom: spacing.lg,
  },
  tagTypeTitle: {
    fontWeight: fontWeight.semibold,
    marginBottom: spacing.sm + 2,
  },
  tagTypeList: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  tagButton: {
    height: 32,
    borderRadius: radius.pill,
    paddingHorizontal: 14,
    justifyContent: 'center',
  },
})
