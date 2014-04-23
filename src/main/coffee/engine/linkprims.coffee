define(->

  class LinkPrims

    constructor: (@world) ->
      @self    = @world.agentSet.self
      @shuffle = @world.agentSet.shuffle

    createLinkFrom: (other) -> @world.createDirectedLink(other, @self())
    createLinksFrom: (others) -> @world.createReverseDirectedLinks(@self(), @shuffle(others))
    createLinkTo: (other) -> @world.createDirectedLink(@self(), other)
    createLinksTo: (others) -> @world.createDirectedLinks(@self(), @shuffle(others))
    createLinkWith: (other) -> @world.createUndirectedLink(@self(), other)
    createLinksWith: (others) -> @world.createUndirectedLinks(@self(), @shuffle(others))

)