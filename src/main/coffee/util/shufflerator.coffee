# (C) Uri Wilensky. https://github.com/NetLogo/Tortoise

define(['shim/random', 'util/iterator'], (Random, Iterator) ->

  class Shufflerator extends Iterator

    _i:       undefined # Number
    _nextOne: undefined # T

    # [T] @ (Array[T]) => Shufflerator
    constructor: (items) ->
      super(items)
      @_i       = 0
      @_nextOne = null

      @_fetch()

    # [U] @ ((T) => U) => Array[U]
    map: (f) ->
      acc = []
      while @_hasNext()
        acc.push(f(@_next()))
      acc

    # ((T) => Unit) => Unit
    forEach: (f) ->
      while @_hasNext()
        f(@_next())

    # () => Array[T]
    toArray: ->
      acc = []
      while @_hasNext()
        acc.push(@_next())
      acc

    # () => Boolean
    _hasNext: ->
      @_i <= @_items.length

    # () => T
    _next: ->
      result = @_nextOne
      @_fetch()
      result

    ###
      Note to self: You see this.  You hate this.  You want this to die.  However, it's not that simple.
      Yes, it's true; this is a translation of the shufflerator from 'headless'.  Yes, that shufflerator
      is insane like this and needlessly pre-iterates the agentset.  It's all true.  But changing this
      to be sane isn't easy.  We need to keep in sync with JVM NetLogo's RNG.  If we don't pre-poll
      the RNG, we get out of sync.  If we _only_ prepoll, we get wrong results, since the random number
      is based on the rest of what `_fetch` is doing with `_i`.  Therefore, we _must_ pre-fetch.

      This isn't a battle that you can't win.  Lament it and move on.  --JAB (5/27/14)
    ###
    # () => Unit
    _fetch: ->
      if @_hasNext()
        if @_i < @_items.length - 1
          randNum = @_i + Random.nextInt(@_items.length - @_i)
          @_nextOne = @_items[randNum]
          @_items[randNum] = @_items[@_i]
        else
          @_nextOne = @_items[@_i]
        @_i++
      else
        @_nextOne = null

      return

)
