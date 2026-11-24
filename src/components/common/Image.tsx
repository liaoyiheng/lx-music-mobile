import { useApple } from '@/theme/apple'
import { BorderRadius } from '@/theme'
import { createStyle } from '@/utils/tools'
import { type ComponentProps, memo, useCallback, useEffect, useMemo, useState } from 'react'
import { View, type ViewProps, StyleSheet, Image as FastImage } from 'react-native'
import { IonIcon } from './Icon'
import { useLayout } from '@/utils/hooks'

export interface ImageProps extends ViewProps {
  style: ComponentProps<typeof FastImage>['style']
  url?: string | number | null
  cache?: boolean
  resizeMode?: ComponentProps<typeof FastImage>['resizeMode']
  blurRadius?: number
  onError?: (url: string | number) => void
}


export const defaultHeaders = {
  'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36',
}

const EmptyPic = memo(({ style, nativeID }: { style: ImageProps['style'], nativeID: ImageProps['nativeID'] }) => {
  const apple = useApple()
  const { onLayout, width } = useLayout()
  const iconSize = Math.max(16, Math.min(48, Math.round((width || 48) * 0.42)))

  return (
    <View
      style={StyleSheet.compose({
        ...styles.emptyPic,
        backgroundColor: apple.isDark ? '#2C2C2E' : '#E5E5EA',
      }, style)}
      onLayout={onLayout}
      nativeID={nativeID}
    >
      <IonIcon name="musical-notes" rawSize={iconSize} color={apple.isDark ? 'rgba(235,235,245,0.34)' : 'rgba(60,60,67,0.32)'} />
    </View>
  )
})

const Image = memo(({ url, cache, resizeMode = 'cover', blurRadius, style, onError, nativeID }: ImageProps) => {
  const [isError, setError] = useState(false)
  const handleError = useCallback(() => {
    setError(true)
    onError?.(url!)
  }, [onError, url])
  useEffect(() => {
    setError(false)
  }, [url])
  const uri = typeof url == 'number'
    ? FastImage.resolveAssetSource(url).uri
    : url?.startsWith('/')
      ? 'file://' + url
      : url
  const showDefault = useMemo(() => !uri || isError, [isError, uri])
  return (
    showDefault ? <EmptyPic style={style} nativeID={nativeID} />
      : (
          <FastImage
            style={style}
            source={{
              uri: uri!,
              headers: defaultHeaders,
              cache: cache === false ? 'reload' : 'force-cache',
            }}
            blurRadius={blurRadius}
            onError={handleError}
            resizeMode={resizeMode}
            nativeID={nativeID}
          />
        )
  )
}, (prevProps, nextProps) => {
  return prevProps.url == nextProps.url &&
    prevProps.style == nextProps.style &&
    prevProps.blurRadius == nextProps.blurRadius &&
    prevProps.nativeID == nextProps.nativeID
})

export const getSize = (uri: string, success: (width: number, height: number) => void, failure?: (error: any) => void) => {
  FastImage.getSize(uri, success, failure)
}
export const clearMemoryCache = async() => {
  // noop
}
export default Image

const styles = createStyle({
  emptyPic: {
    borderRadius: BorderRadius.normal,
    alignItems: 'center',
    justifyContent: 'center',
  },
})
