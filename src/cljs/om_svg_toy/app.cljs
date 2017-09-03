(ns om-svg-toy.app
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [cljsjs.react-motion]))

(def spring js/ReactMotion.spring)
(def Motion (js/React.createFactory js/ReactMotion.Motion))

(defn date->data [d]
  {:seconds (.getSeconds d)
   :minutes (.getMinutes d)
   :hours (.getHours d)})

(defn r
  [deg]
  (str "rotate(" deg " 50 50)"))

(defn- tick
  [app]
  (om/transact! app :now (fn [_] (date->data (new js/Date)))))

(defn widget [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (om/set-state! owner :interval (js/setInterval #(tick app) 16)))
    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :interval)))
    om/IRender
    (render [this]
      (let [now (:now app)]
        (dom/svg #js {:id "clock" :viewbox "0 0 100 100"}
                 (dom/circle #js {:id "face" :cx "50" :cy "50" :r "45"})
                 (dom/g #js {:id "hands"}
                   (dom/rect #js {:id "hour" :x "48.5" :y "12.5" :width "5" :height "40" :rx "2.5" :ry "2.55"
                                  :transform (r (+ (* 30 (mod (:hours now) 12))
                                                   (/ (:minutes now) 2)))})
                   (dom/rect #js {:id "min" :x "48" :y "12.5" :width "3" :height "40" :rx "2" :ry "2"
                                  :transform (r (* 6 (:minutes now)))})
                   (dom/line #js {:id "sec" :x1 "50" :y1 "50" :x2 "50" :y2 "16"
                                       :transform (r (* 6 (:seconds now)))})))))))

(defn splat-path
  [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/path
        #js {:ref "splat-path"
             :fill "#FFFFFF"
             :stroke "#000000"
             "strokeWidth" "4"
             "strokeMiterlimit" "10"
             "strokeDasharray" (some-> (om/get-ref owner "splat-path")
                                       (.getTotalLength))
             "strokeDashoffset" (* (:offset app)
                                   (or (some-> (om/get-ref owner "splat-path")
                                           (.getTotalLength))
                                       0))
             :d "M66.039,133.545c0,0-21-57,18-67s49-4,65,8s30,41,53,27s66,4,58,32s-5,44,18,57s22,46,0,45s-54-40-68-16s-40,88-83,48s11-61-11-80s-79-7-70-41C46.039,146.545,53.039,128.545,66.039,133.545z"}))))

(defn splat [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/svg
        #js {:version "1.1"
             :id "Layer_1"
             :xmlns "http://www.w3.org/2000/svg"
             :xmlnsXlink "http://www.w3.org/1999/xlink"
             :x "0px"
             :y "0px"
             :width "340px"
             :height "333px"
             :viewBox "0 0 340 333"
             :enableBackground "new 0 0 340 333"
             :xmlSpace "preserve"}
        (Motion
          #js {:defaultStyle #js {:offset 1}
               :style #js {:offset (spring 0 #js {:stiffness 10
                                                  :damping 6})}}
          (fn [interpolate-style]
            (om/build splat-path {:offset (.-offset interpolate-style)})))))))

(defn %
  [n]
  (str (* 100 n) "%"))

(defn range-slider
  [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/svg
        #js {:width 500
             :height 200
             :xmlns "http://www.w3.org/2000/svg"}
        (dom/defs
          nil
          (dom/linearGradient
            #js {:id "ActiveTime"
                 :gradientUnits "userSpaceOnUse"}
            (dom/stop #js {:offset "0%" :stopColor "#02aab0"})
            (dom/stop #js {:offset "100%" :stopColor "#00cdac"}))

          (dom/linearGradient
            #js {:id "BusyTime"
                 :gradientUnits "userSpaceOnUse"}
            (dom/stop #js {:offset "5%" :stopColor "#d31027"})
            (dom/stop #js {:offset "95%" :stopColor "#ea384d"}))

          (dom/mask
            #js {:id "busy"}
            (for [{:keys [start end k]} [{:start (/ 12 24)
                                          :end (/ 13 24)
                                          :k "busy-1"}
                                         {:start (/ 15 24)
                                          :end (/ 17 24)
                                          :k "busy-2"}]]
              (dom/rect #js {:x (% start)
                             :y 10
                             :key k
                             :width (% (- end start))
                             :height 10
                             :fill "white"
                             :onClick (fn []
                                        (js/alert "hi"))}))
            )
          (dom/mask
            #js {:id "available"}
            (for [{:keys [start end k]} [{:start (/ 9 24) :end (/ 11.5 24) :k 1}
                                         {:start (/ 13 24) :end (/ 14 24) :k 2}
                                         {:start (/ 18 24) :end (/ 19 24) :k 3}]]
              (dom/rect #js {:x (% start)
                             :y 10
                             :key k
                             :width (% (- end start))
                             :height 10
                             :fill "white"}))))
        (dom/rect #js {:fill "lightgrey"
                       :x 0
                       :y 10
                       :width (% 1)
                       :height 10})
        (dom/rect #js {:fill "url(#ActiveTime)"
                       :x 0
                       :y 10
                       :width (% 1)
                       :height 10
                       :mask "url(#available)"})
        (for [{:keys [start end k msg]}
              [{:start (/ 12 24)
                :end (/ 13 24)
                :k "busy-1"
                :msg "Lunch"}
               {:start (/ 15 24)
                :end (/ 17 24)
                :k "busy-2"
                :msg "Yoga"}]]
          (dom/rect #js {:x (% start)
                         :y 10
                         :key k
                         :width (% (- end start))
                         :height 10
                         :fill "url(#BusyTime)"
                         :onMouseEnter (fn []
                                         (js/alert msg))}))))))

(defn page
  [app owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (om/build widget app)
        (om/build splat app)
        (om/build range-slider app)))))

(def app-state (atom {:now {:seconds 0
                            :minutes 0
                            :hours 0}
                      :splat {:path-length 0}}))

(defn init []
  (om/root page app-state
           {:target (. js/document (getElementById "container"))}))
