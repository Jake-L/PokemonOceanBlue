package pokemonoceanblue;

public class DayCareConversationModel extends ConversationModel
{
    public DayCareModel dayCareModel;

    public DayCareConversationModel(DayCareModel dayCareModel)
    {
        super(9998, null, null, false);
        this.dayCareModel = dayCareModel;
        this.loadEvents();
    }

    /**
     * Manually create the conversation since it's dynamic based on the Pokemon in the day care
     */
    private void loadEvents()
    {
        // events when no Pokemon is deposited
        if (dayCareModel.pokemon[0] == null && dayCareModel.pokemon[1] == null)
        {
            this.events.add(new ConversationEvent(0, 1, "You have no Pokemon deposited. Please select a Pokemon.", -1));
            this.events.add(new ConversationEvent(1, 2, null, -1));
            this.events.get(1).openParty = true;
        }
        // events when two Pokemon are deposited
        else if (dayCareModel.pokemon[0] != null && dayCareModel.pokemon[1] != null)
        {
            this.events.add(new ConversationEvent(0, 1, "We are currently taking care of two of your Pokemon. Would you like one back?", -1));
            this.events.get(0).setOptions(
                new String[]{dayCareModel.pokemon[0].name, dayCareModel.pokemon[1].name, "CANCEL"}, 
                new int[]{1, 2, 3}
            );
            this.events.add(new ConversationEvent(1, -1, "Here is your " + dayCareModel.pokemon[0].name + " back.", 0));
            this.events.add(new ConversationEvent(2, -1, "Here is your " + dayCareModel.pokemon[1].name + " back.", 1));
            this.events.add(new ConversationEvent(3, -1, "Come back any time.", -1));
        }
        // events when one Pokemon is deposited
        else 
        {
            this.events.add(new ConversationEvent(0, 1, "We are currently taking care of one of your Pokemon. What would you like to do?", -1));
            this.events.get(0).setOptions(
                new String[]{"DEPOSIT", "WITHDRAW", "CANCEL"}, 
                new int[]{1, 2, 3}
            );
            // deposit events
            this.events.add(new ConversationEvent(1, 2, null, -1));
            this.events.get(1).openParty = true;
            // withdraw events
            int pokemonIndex = dayCareModel.pokemon[0] == null ? 1 : 0;
            this.events.add(new ConversationEvent(2, -1, "Here is your " + dayCareModel.pokemon[pokemonIndex].name + " back.", pokemonIndex));
            this.events.add(new ConversationEvent(3, -1, "Come back any time.", -1));
        }
    }
    
    /**
     * Leave a Pokemon add the day care
     * @param pokemon the Pokemon to be deposited at the day care
     * @return true if the Pokemon is not null and therefore should be removed from the player's party
     */
    @Override
    public boolean setPokemon(PokemonModel pokemon)
    {
        this.events.clear();

        if (pokemon == null)
        {
            this.events.add(new ConversationEvent(0, -1, "Come back any time.", -1));
            return false;
        }
        else
        {
            this.events.add(new ConversationEvent(0, -1, "Thanks, we'll take care of your " + pokemon.name + ".", -1));
            this.dayCareModel.setPokemon(pokemon);
            return true;
        }
    }
}