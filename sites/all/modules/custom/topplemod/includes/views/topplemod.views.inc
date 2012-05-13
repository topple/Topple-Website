<?php

/**
 * 
 * Exposes store data to views
 */
function topplemod_views_data(){
  
  //exposes {users_store} table to views
  $data['users_store']['table']['group'] = t('Users Store');
  
  $data['users_store']['table']['base'] = array(
    'field' => 'uid',
    'title' => 'Users Stores',
    'help' => 'Exposes store information to views',
    'weight' => -100,
  );
  
  $data['users_store']['uid'] = array(
    'title' => 'Store ID',
    'help' => 'The user ID assigned to a store',
    'field' => array('handler' => 'views_handler_field'),
    'argument' => array('handler' => 'views_handler_argument_numeric'),
    'filter' => array('handler' => 'views_handler_filter_numeric'),
    'sort' => array('handler' => 'views_handler_sort_numeric'),
    'relationship' => array(
      'handler' => 'views_handler_relationship',
      'base' => 'users',
      'field' => 'uid',
      'label' => t('Users store to user'),
    ),
  );
  
  $data['users_store']['storename'] = array(
    'title' => 'Store Name',
    'help' => 'The name of the store',
    'field' => array('handler' => 'views_handler_field'),
    'argument' => array('handler' => 'views_handler_argument_string'),
    'filter' => array('handler' => 'views_handler_filter_string'),
    'sort' => array('handler' => 'views_handler_sort'),
  );
  
  $data['users_store']['score'] = array(
    'title' => 'Score',
    'help' => 'The current topple score of a store',
    'field' => array('handler' => 'views_handler_field'),
    'argument' => array('handler' => 'views_handler_argument_numeric'),
    'filter' => array('handler' => 'views_handler_filter_numeric'),
    'sort' => array('handler' => 'views_handler_sort'),
  
  );
  
  return $data;  
}  
  

