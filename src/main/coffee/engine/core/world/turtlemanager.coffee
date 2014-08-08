# (C) Uri Wilensky. https://github.com/NetLogo/Tortoise

define(['engine/core/nobody', 'engine/core/turtle', 'engine/core/turtleset', 'engine/core/structure/builtins'
      , 'engine/core/world/idmanager', 'shim/random', 'util/colormodel', 'util/exception']
     , ( Nobody,               Turtle,               TurtleSet,               Builtins
      ,  IDManager,                     Random,        ColorModel,        Exception) ->

  class TurtleManager

    _turtleIDManager: undefined # IDManager
    _turtles:         undefined # Array[Turtle]
    _turtlesById:     undefined # Object[Number, Turtle]

    # (World, Updater, BreedManager) => TurtleManager
    constructor: (@_world, @_breedManager, @_updater) ->
      @_turtleIDManager = new IDManager
      @_turtles         = []
      @_turtlesById     = {}

    # () => Unit
    clearTurtles: ->
      @turtles().forEach((turtle) ->
        try
          turtle.die()
        catch error
          throw error if not (error instanceof Exception.DeathInterrupt)
        return
      )
      @_turtleIDManager.reset()
      return

    # (Number, String) => TurtleSet
    createOrderedTurtles: (n, breedName) ->
      turtles = _(0).range(n).map(
        (num) =>
          color   = ColorModel.nthColor(num)
          heading = (360 * num) / n
          @createTurtle(color, heading, 0, 0, @_breedManager.get(breedName))
      ).value()
      new TurtleSet(turtles, breedName)

    # (Number, Number, Number, Number, Breed, String, Number, Boolean, Number, PenManager) => Turtle
    createTurtle: (color, heading, xcor, ycor, breed, label, lcolor, isHidden, size, penManager) ->
      id     = @_turtleIDManager.next()
      turtle = new Turtle(@_world, id, @_updater.updated, @_updater.registerDeadTurtle, color, heading, xcor, ycor, breed, label, lcolor, isHidden, size, penManager)
      @_updater.updated(turtle)(Builtins.turtleBuiltins...)
      @_turtles.push(turtle)
      @_turtlesById[id] = turtle
      turtle

    # (Number, String, Number, Number) => TurtleSet
    createTurtles: (n, breedName, xcor = 0, ycor = 0) ->
      turtles = _(0).range(n).map(=>
        color   = ColorModel.randomColor()
        heading = Random.nextInt(360)
        @createTurtle(color, heading, xcor, ycor, @_breedManager.get(breedName))
      ).value()
      new TurtleSet(turtles, breedName)

    # (Number) => Agent
    getTurtle: (id) ->
      @_turtlesById[id] or Nobody

    # (String, Number) => Agent
    getTurtleOfBreed: (breedName, id) ->
      turtle = @getTurtle(id)
      if turtle.getBreedName().toUpperCase() is breedName.toUpperCase()
        turtle
      else
        Nobody

    # (Number) => Unit
    removeTurtle: (id) ->
      turtle = @_turtlesById[id]
      @_turtles.splice(@_turtles.indexOf(turtle), 1)
      delete @_turtlesById[id]
      return

    # () => TurtleSet
    turtles: ->
      new TurtleSet(@_turtles)

    # (String) => TurtleSet
    turtlesOfBreed: (breedName) =>
      breed = @_breedManager.get(breedName)
      new TurtleSet(breed.members, breedName)

    # () => Unit
    _clearTurtlesSuspended: ->
      @_turtleIDManager.suspendDuring(() => @clearTurtles())
      return

)