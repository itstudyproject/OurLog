package com.example.ourLog.service;

import com.example.apiserver.dto.*;
import com.example.apiserver.entity.Work;
import com.example.apiserver.entity.Members;
import com.example.apiserver.entity.Photos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface WorkService {
  PageResultDTO<JournalDTO, Object[]> getList(PageRequestDTO pageRequestDTO);

  Long register(JournalDTO journalDTO);

  JournalDTO get(Long jno);

  void modify(JournalDTO journalDTO);

  List<String> removeWithCommentsAndPhotos(Long jno);

  void removePhotosbyUUID(String uuid);

  default Map<String, Object> dtoToEntity(JournalDTO journalDTO) {
    System.out.println(">>>"+journalDTO);
    Map<String, Object> entityMap = new HashMap<>();

    Work work = Work.builder()
        .jno(journalDTO.getJno())
        .title(journalDTO.getTitle())
        .content(journalDTO.getContent())
        .members(Members.builder().mid(journalDTO.getMembersDTO().getMid()).build())
        .build();
    System.out.println(">>>"+work);
    entityMap.put("work", work);

    List<PhotosDTO> photosDTOList = journalDTO.getPhotosDTOList();
    if (photosDTOList != null && photosDTOList.size() > 0) {
      List<Photos> photosList = photosDTOList.stream().map(photosDTO -> {
        Photos photos = Photos.builder()
            .path(photosDTO.getPath())
            .photosName(photosDTO.getPhotosName())
            .uuid(photosDTO.getUuid())
            .work(work)
            .build();
        return photos;
      }).collect(Collectors.toList());
      entityMap.put("photosList", photosList);
    }
    return entityMap;
  }

  default JournalDTO entityToDTO(Work work, List<Photos> photosList,
                                 Members members,Long likes, Long commentsCnt) {

    MembersDTO membersDTO = MembersDTO.builder()
        .mid(members.getMid())
        .name(members.getName())
        .email(members.getEmail())
        .nickname(members.getNickname())
        .mobile(members.getMobile())
        .build();
    JournalDTO journalDTO = JournalDTO.builder()
        .jno(work.getJno())
        .title(work.getTitle())
        .content(work.getContent())
        .membersDTO(membersDTO)
        .regDate(work.getRegDate())
        .modDate(work.getModDate())
        .build();
    List<PhotosDTO> photosDTOList = new ArrayList<>();
    if (photosList.size() > 0 && photosList.get(0) != null) {
      photosDTOList = photosList.stream().map(photos -> {
        PhotosDTO photosDTO = PhotosDTO.builder()
            .photosName(photos.getPhotosName())
            .path(photos.getPath())
            .uuid(photos.getUuid())
            .build();
        return photosDTO;
      }).collect(Collectors.toList());
    }
    journalDTO.setPhotosDTOList(photosDTOList);
    journalDTO.setLikes(likes);
    journalDTO.setCommentsCnt(commentsCnt);
    return journalDTO;
  }

}