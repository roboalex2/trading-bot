package at.discord.bot.mapper;

import at.discord.bot.persistent.model.AssetHistoryEntity;
import at.discord.bot.model.asset.AssetHistoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetHistoryMapper {

    // Mapping from AssetHistoryEntity to AssetHistoryDTO
    @Mapping(source = "captureTimestamp", target = "timestamp")
    AssetHistoryDTO entityToDTO(AssetHistoryEntity entity);
}
