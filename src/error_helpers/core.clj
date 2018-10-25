(ns error-helpers.core)

(defmacro ok-let [ok? unwrap-ok make-ok bindings & body]
  (cond (zero? (count bindings))      `(~make-ok (do ~@body))
        (odd? (count bindings)) (throw (IllegalArgumentException. (str bindings " number of bindings must be even")))
        (symbol? (bindings 0))        `(let ~(subvec bindings 0 2 )
                                         (let [b0#    ~(bindings 0)
                                               ok-b0# (~ok? b0#)]
                                           (if ok-b0#
                                             (let [~(bindings 0) (~unwrap-ok b0#)]
                                               (ok-let ~ok? ~unwrap-ok ~make-ok ~(subvec bindings 2) ~@body))
                                             b0#)))
        :else (throw (IllegalArgumentException. "binding must be a symbol"))))

(defmacro if-pred [pred? bindings then else]
  (when (not= 2 (count bindings)) (throw (IllegalArgumentException. (str bindings " number of bindings must be 2"))))
  (let [form (bindings 0) tst (bindings 1)]
    `(let [t# ~tst]
       (if (~pred? t#)
         (let [~form t#]
           ~then)
         ~else))))

(defn- error-helper [f ok-key err-key err-message m]
  `(try
     {~ok-key ~f}
     (catch Exception e#
       (merge {~err-key [~err-message e#]} ~m))))

(defmacro with-error [f {:keys [ok-key err-key err-message context] :or {context {}}}]
  (error-helper f ok-key err-key err-message context))
