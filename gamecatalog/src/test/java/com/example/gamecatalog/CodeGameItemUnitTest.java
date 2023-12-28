package com.example.gamecatalog;

import com.example.gamecatalog.entities.CodeGameItem;
import com.example.gamecatalog.entities.Game;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

 class CodeGameItemUnitTest {

    @Test
    void selectedItemChange(){
        Game game = Game.builder().name("Fifa 23").description("Football/Soccer simulation game").price(69.99F).build();

        CodeGameItem item = CodeGameItem.builder().game(game).code("ADASD-ASDASD-ASDAD").platform("STEAM").build();
        item.setSelected(true);
        assertThat(item.getSelected()).isEqualTo(true);
        System.out.println("Se ha verificado que se ha cambiado de estado ("+item.getSelected().toString()+") correctamente");
    }

}
